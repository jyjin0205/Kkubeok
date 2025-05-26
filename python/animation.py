import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.spatial.transform import Rotation as R
from mpl_toolkits.mplot3d import Axes3D
from matplotlib.animation import FuncAnimation



def determineDirections(x, y, z):
    bx = 1 if x >= 0 else 0
    by = 1 if y >= 0 else 0
    bz = 1 if z >= 0 else 0

    direction = (bx << 2) | (by << 1) | bz
    return direction


# --- 1. 데이터 로딩 -------
datas = ["data/jumi_Lean Back_gyro.csv", "data/Yujin_Lean Back_gyro.csv", "data/jumi_Resting Head(Left)_gyro.csv",
         "data/Yujin_Resting Head(Left)_gyro.csv", "data/jumi_Resting Head(Right)_gyro.csv",
         "data/Yujin_Resting Head(Right)_gyro.csv"]

gravity_datas = ["data/jumi_Lean Back_gravity.csv", "data/Yujin_Lean Back_gravity.csv",
                 "data/jumi_Resting Head(Left)_gravity.csv",
                 "data/Yujin_Resting Head(Left)_gravity.csv", "data/jumi_Resting Head(Right)_gravity.csv",
                 "data/Yujin_Resting Head(Right)_gravity.csv"]
# ---- 인덱스 결정 --------
index = 0
d = datas[index]
gd = gravity_datas[index]

df = pd.read_csv(d)
df = df.sort_values('timestamp')
df['dt'] = df['timestamp'].diff().fillna(0) / 1000.0  # ms → s

direction_counts = {i: 0 for i in range(8)}

# 쿼터니언 리스트 초기화
quaternions = [R.identity()]

quaternion = R.identity()
start_vector = np.array([0, 0, 0])

gdf = pd.read_csv(gd)
if gdf.loc[1, 'x'] > 6:
    start_vector += np.array([0, -1, 0])
if gdf.loc[1, 'x'] < -6:
    start_vector += np.array([0, +1, 0])
if gdf.loc[1, 'y'] > 6:
    start_vector += np.array([1, 0, 0])
if gdf.loc[1, 'y'] < -6:
    start_vector += np.array([-1, 0, 0])

if np.linalg.norm(start_vector) == 0:
    start_vector = np.array([0, 0, 1])

start_vector = start_vector / np.linalg.norm(start_vector)

# --- 2. 쿼터니언 누적 계산 ---
for i in range(1, len(df)):
    omega = df.loc[i, ['x', 'y', 'z']].values  # rad/s
    dt = df.loc[i, 'dt']
    delta_rot = R.from_rotvec(omega * dt)  # 회전 객체
    cumulative_rot = quaternions[-1] * delta_rot  # 누적 회전

    # !-----without simulation-----!#
    quaternions.append(cumulative_rot)

# numpy 배열로 변환
quaternions = np.array(quaternions)
# --- 3. 시각화 준비 ---
fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')
ax.set_xlim([-1, 1])
ax.set_ylim([-1, 1])
ax.set_zlim([-1, 1])
ax.set_xlabel("X")
ax.set_ylabel("Y")
ax.set_zlabel("Z")
ax.set_title("Head Rotation Over Time (Quaternion-based)")

# 초기 화살표
quiver = ax.quiver(0, 0, 0, 0, 0, 1, length=1.0, color='r')

# --- 4. 애니메이션 업데이트 함수 ---
def update(frame):
    global quiver
    quiver.remove()

    rot = quaternions[frame]
    rotated_vector = rot.apply(start_vector)  # z축 기준 회전

    end_x, end_y, end_z = rotated_vector
    direction = determineDirections(end_x,end_y,end_z)
    direction_counts[direction] += 1

    quiver = ax.quiver(0, 0, 0, *rotated_vector, length=1.0, color='r')
    return quiver,

# --- 5. 애니메이션 실행 ---
ani = FuncAnimation(fig, update, frames=range(0, len(df), 10), interval=5, blit=False)
plt.show()



