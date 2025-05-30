import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix, classification_report
import matplotlib.pyplot as plt
from libsvm.svmutil import *
import json
import seaborn as sns
import os
from scipy.spatial.transform import Rotation as R

WINDOW_SIZE = 200
STRIDE = 100

def load_csv_data(filepath):
    df = pd.read_csv(filepath, header= 0)
    df.columns = ['timestamp', 'x','y','z']
    return df

def process_all_csv(name, label):
    features_list = []
    file_name = name +"_" + label
    gravity = load_csv_data(os.path.join("data", file_name + '_gravity.csv'))
    gyro = load_csv_data(os.path.join("data", file_name + '_gyro.csv'))
    linear = load_csv_data(os.path.join("data",file_name + '_accel.csv'))

    length = min(len(gravity), len(gyro), len(linear))

    start_vector, extract_quaternions = extract_direction(name, label)

    for start in range(0, length - WINDOW_SIZE, STRIDE):
        g_feat = extract_features(gravity.iloc[start:start + WINDOW_SIZE])
        r_feat = extract_features(gyro.iloc[start:start + WINDOW_SIZE])
        l_feat = extract_features(linear.iloc[start:start + WINDOW_SIZE])
        all_feat = g_feat + r_feat + l_feat  # 총 18개
        rotated_vector = extract_quaternions[start].apply(start_vector)
        full_feat = all_feat + rotated_vector.tolist()

        features_list.append(full_feat)

    return features_list

def extract_direction(name,label):
    file_name = name + "_"+ label
    gravity = load_csv_data(os.path.join("data",file_name+"_gravity.csv"))
    gyro = load_csv_data(os.path.join("data", file_name + '_gyro.csv'))

    gyro['timestamp'] = gyro['timestamp'].map(float)
    gyro['dt'] = gyro['timestamp'].diff().fillna(0) / 1000.0  # ms → s

    start_vector = np.array([0, 0, 0])
    quaternions = [R.identity()]


    if float(gravity.loc[1, 'x']) > 6:
        start_vector += np.array([0, -1, 0])
    if float(gravity.loc[1, 'x']) < -6:
        start_vector += np.array([0, +1, 0])
    if float(gravity.loc[1, 'y']) > 6:
        start_vector += np.array([1, 0, 0])
    if float(gravity.loc[1, 'y']) < -6:
        start_vector += np.array([-1, 0, 0])

    if np.linalg.norm(start_vector) == 0:
        start_vector = np.array([0, 0, 1])

    start_vector = start_vector / np.linalg.norm(start_vector)

    for i in range(1, len(gyro)):
        omega = gyro.loc[i, ['x', 'y', 'z']].values  # rad/s
        dt = gyro.loc[i, 'dt']
        delta_rot = R.from_rotvec(omega * dt)  # 회전 객체
        cumulative_rot = quaternions[-1] * delta_rot  # 누적 회전

        # !-----without simulation-----!#
        quaternions.append(cumulative_rot)

        # numpy 배열로 변환
    quaternions = np.array(quaternions)

    return start_vector,quaternions



def extract_features(df):
    features =[]
    for axis in ['x','y','z']:
        values = pd.to_numeric(df[axis], errors='coerce').dropna().values
        features.append(np.mean(values))
        features.append(np.std(values))
    energy = np.sum(df[['x', 'y', 'z']].apply(pd.to_numeric, errors='coerce').dropna().values ** 2)
    features.append(energy)
    return features



names = ["jumi","Yujin"]
activities = ["Lean Back", "Resting Head(Left)", "Resting Head(Right)", "Nodding Off", "Others"]
label_map = {activity: idx for idx, activity in enumerate(activities)}


jumiLabel =[]
yujinLabel =[]

jumiData = []
yujinData = []


for activity in activities:
    feats = process_all_csv("jumi", activity)
    jumiData.extend(feats)
    jumiLabel.extend([label_map[activity]] * len(feats))

X = np.array(jumiData)
Y = np.array(jumiLabel)

# 데이터 분할
X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=0.3, random_state=42)

# 정규화 (train 기준)
X_min = X_train.min(axis=0)
X_max = X_train.max(axis=0)

X_train_scaled = 2 * (X_train - X_min) / (X_max - X_min + 1e-8) - 1
X_test_scaled = 2 * (X_test - X_min) / (X_max - X_min + 1e-8) - 1

# libsvm 형식으로 변환
def to_libsvm_format(X_scaled):
    return [{i + 1: val for i, val in enumerate(row)} for row in X_scaled]

X_train_libsvm = to_libsvm_format(X_train_scaled)
X_test_libsvm = to_libsvm_format(X_test_scaled)


model = svm_train(y_train.tolist(), X_train_libsvm, '-s 0 -t 2')
svm_save_model('jumi_activity_with_direction_model.model', model)


norm_params = {
    'X_min': X_min.tolist(),
    'X_max': X_max.tolist()
}

with open('jumi_norm_params_with_direction.json', 'w') as f:
    json.dump(norm_params, f)

pred_labels, acc, _ = svm_predict(y_test.tolist(), X_test_libsvm, model)

print(f'\n✅ Accuracy: {acc[0]:.2f}%\n')

# Confusion matrix
cm = confusion_matrix(y_test, pred_labels)
labels = list(range(5))

plt.figure(figsize=(8, 6))
sns.heatmap(cm, annot=True, fmt='d', cmap='Blues', xticklabels=labels, yticklabels=labels)
plt.xlabel('Predicted')
plt.ylabel('True')
plt.title('Confusion Matrix')
plt.show()

# Classification Report
print("\n🔍 Classification Report:\n")
print(classification_report(y_test, pred_labels, digits=3))