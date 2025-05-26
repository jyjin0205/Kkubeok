import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import confusion_matrix, classification_report
import matplotlib.pyplot as plt
from libsvm.svmutil import *

WINDOW_SIZE = 200
STRIDE = 100

# --- 1. 데이터 로딩 ---
datas = ["data/jumi_Lean Back_gyro.csv", "data/Yujin_Lean Back_gyro.csv", "data/jumi_Resting Head(Left)_gyro.csv",
         "data/Yujin_Resting Head(Left)_gyro.csv", "data/jumi_Resting Head(Right)_gyro.csv",
         "data/Yujin_Resting Head(Right)_gyro.csv", "data/Yujin_Nodding Off_gyro.csv","data/jumi_Nodding Off_gyro.csv"]

def extract_features(df):
    features =[]
    for axis in ['x','y','z']:
        values = df[axis].values
        features.append(np.mean(values))
        features.append(np.std(values))
        features.append(np.max(values))
        features.append(np.min(values))
    energy = np.sum(df[['x', 'y', 'z']].values ** 2)
    features.append(energy)
    return features

Label = []
Feature = []
for d in datas:
    df = pd.read_csv(d)
    for start in range(0, len(df)-WINDOW_SIZE, STRIDE):
        if "Nodding Off" in d:
            Label.append(1)
        else:
            Label.append(0)
        features = extract_features(df.iloc[start:start + WINDOW_SIZE])
        Feature.append(features)

X = np.array(Feature)
Y = np.array(Label)

# 데이터 분할
X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=0.3, random_state=42)

def to_libsvm_format(X_scaled):
    return [{i + 1: val for i, val in enumerate(row)} for row in X_scaled]

# SVM 모델 생성 (기본은 선형 커널 아님)
X_train_format = to_libsvm_format(X_train)
X_test_format = to_libsvm_format(X_test)

model = svm_train(y_train.tolist(), X_train_format, '-s 0 -t 2')

pred_labels, acc, _ = svm_predict(y_test.tolist(), X_test_format, model)

print(f'\n✅ Accuracy: {acc[0]:.2f}%\n')



