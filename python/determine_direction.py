import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.spatial.transform import Rotation as R
from mpl_toolkits.mplot3d import Axes3D
from matplotlib.animation import FuncAnimation

"""
delta
(+++) 고개 왼쪽꺾, 뒤로 누움, 왼쪽으로 돌림 7
(++ -) 고개 왼쪽꺾, 뒤로 누움, 오른쪽으로 돌림 6
(+ - +) 고개 왼쪽꺾, 앞으로 숙임, 왼쪽으로 돌림 5
(+ - -) 고개 왼쪽꺾, 앞으로 숙임, 오른쪽으로 돌림 4
(- ++) 고개 오른쪽꺾, 뒤로 누움, 왼쪽으로 돌림 3
(- + -) 고개 오른쪽꺾, 뒤로 누움, 오른쪽으로 돌림 2
(- - +) 고개 오른쪽꺾, 앞으로 숙임, 왼쪽으로 돌림 1
(- - -) 고개 오른쪽꺾, 앞으로 숙임, 오른쪽으로 돌림 0

화살표
y + 앞쪽
y - 뒤쪽
direction
0 x- y- z- 왼쪽
1 x- y- z+
2 x- y+ z-
3 x- y+ z+
4 x+ y- z- 오른쪽
5 x+ y- z+
6 x+ y+ z-
7 x+ y+ z+

"""


def determineDirections(x, y, z):
    bx = 1 if x >= 0 else 0
    by = 1 if y >= 0 else 0
    bz = 1 if z >= 0 else 0

    direction = (bx << 2) | (by << 1) | bz
    return direction


# --- 1. 데이터 로딩 ---
datas = ["data/jumi_Lean Back_gyro.csv", "data/Yujin_Lean Back_gyro.csv", "data/jumi_Resting Head(Left)_gyro.csv",
         "data/Yujin_Resting Head(Left)_gyro.csv", "data/jumi_Resting Head(Right)_gyro.csv",
         "data/Yujin_Resting Head(Right)_gyro.csv"]

gravity_datas = ["data/jumi_Lean Back_gravity.csv", "data/Yujin_Lean Back_gravity.csv",
                 "data/jumi_Resting Head(Left)_gravity.csv",
                 "data/Yujin_Resting Head(Left)_gravity.csv", "data/jumi_Resting Head(Right)_gravity.csv",
                 "data/Yujin_Resting Head(Right)_gravity.csv"]

for d, gd in zip(datas, gravity_datas):
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

    for i in quaternions:
        rotated_vector = i.apply(start_vector)
        direction = determineDirections(*rotated_vector)
        direction_counts[direction] += 1
    fig, ax = plt.subplots(figsize=(6, 4))
    ax.axis('off')

    table_data = [["Direction", "Count"]]
    for k in range(8):
        table_data.append([str(k), str(direction_counts[k])])

    table = ax.table(cellText=table_data,
                     cellLoc='center',
                     colWidths=[0.3, 0.3],
                     loc='center')

    table.scale(1, 2)  # 세로 크기 조절
    table.auto_set_font_size(False)
    table.set_fontsize(12)

    # 4. 제목 및 출력
    name = d.split("/")[-1]
    parts = name.replace("_gyro.csv", "").split("_", maxsplit=1)
    name = parts[0]
    action = parts[1]
    plt.title(f"{name} : {action}", fontsize=14)
    plt.savefig(f"{name}_{action}.png")

    print("Direction counts after animation:")
    for k, v in direction_counts.items():
        print(f"Direction {k}: {v}")
