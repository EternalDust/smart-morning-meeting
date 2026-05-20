"""
训练总控脚本
从 MySQL 拉取历史晨会数据 → 特征工程 → 训练 IF + LSTM → 保存模型

用法：
  python training/train_pipeline.py
"""
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pandas as pd
import pymysql
from sklearn.preprocessing import StandardScaler
import joblib
import numpy as np

from models.config import (
    MYSQL_HOST, MYSQL_PORT, MYSQL_DB, MYSQL_USER, MYSQL_PASSWORD,
    FEATURE_COLUMNS, SEQUENCE_WINDOW, IF_MODEL_PATH, LSTM_MODEL_PATH, SCALER_PATH,
)
from models.feature_engineering import build_sequences, fit_scaler, normalize
from models.isolation_forest_detector import IFDetector
from models.lstm_detector import LSTMAutoencoder


def fetch_history() -> pd.DataFrame:
    """
    从 MySQL 拉取历史晨会统计数据和督办数据，按天聚合。
    返回包含 FEATURE_COLUMNS 的 DataFrame。
    """
    conn = pymysql.connect(
        host=MYSQL_HOST, port=MYSQL_PORT, database=MYSQL_DB,
        user=MYSQL_USER, password=MYSQL_PASSWORD, charset="utf8mb4",
    )

    # 联表查询：晨会统计 + 督办统计 + 医疗指标，按日期+科室聚合
    sql = """
        SELECT
            m.stat_date,
            m.dept_id,
            m.dept_name,
            m.attend_rate,
            COALESCE(s.solve_rate, 0) AS solve_rate,
            m.late_num,
            m.absent_num,
            COALESCE(s.overdue_count, 0) AS overdue_count,
            COALESCE(s.urgent_count, 0) AS urgent_count,
            m.avg_duration
        FROM bi_stat_meeting m
        LEFT JOIN bi_stat_supervise s
            ON m.stat_date = s.stat_date AND m.dept_id = s.dept_id
        ORDER BY m.stat_date ASC
    """
    df = pd.read_sql(sql, conn)
    conn.close()

    # 填充缺失值
    df[FEATURE_COLUMNS] = df[FEATURE_COLUMNS].fillna(0)

    print(f"从 MySQL 拉取历史数据: {len(df)} 条")
    return df


def main():
    print("=" * 50)
    print("晨会异常检测模型训练管道")
    print("=" * 50)

    # 1. 拉取历史数据
    df = fetch_history()
    if df.empty:
        print("错误：历史数据为空，无法训练")
        sys.exit(1)

    # 2. 特征标准化
    print("\n[1/4] 拟合标准化器...")
    scaler = fit_scaler(df)
    print(f"    标准化器已保存至 {SCALER_PATH}")

    # 3. 训练 Isolation Forest
    print("\n[2/4] 训练 Isolation Forest ...")
    features = scaler.transform(df[FEATURE_COLUMNS].values)
    if_detector = IFDetector()
    if_detector.train(features)
    if_detector.save(IF_MODEL_PATH)
    print(f"    IF 模型已保存至 {IF_MODEL_PATH}")

    # 4. 训练 LSTM Autoencoder
    print("\n[3/4] 构建 LSTM Autoencoder ...")
    X, y = build_sequences(df, SEQUENCE_WINDOW)
    X_norm = scaler.transform(X.reshape(-1, X.shape[-1])).reshape(X.shape)
    print(f"    时间窗口序列: X shape={X_norm.shape}")

    lstm = LSTMAutoencoder()
    lstm.build_model(n_features=len(FEATURE_COLUMNS), window=SEQUENCE_WINDOW)
    print("\n[4/4] 训练 LSTM (可能耗时较长)...")
    lstm.train(X_norm)
    lstm.save(LSTM_MODEL_PATH)
    print(f"    LSTM 模型已保存至 {LSTM_MODEL_PATH}")

    # 5. 模型验证
    print("\n" + "=" * 50)
    print("训练完成！模型验证：")
    if_scores, if_labels = if_detector.predict(features[:10])
    print(f"  IF 样本异常得分 (前10条): {[round(s, 3) for s in if_scores]}")
    print(f"  IF 异常标记数: {sum(if_labels[:10])} / 10")

    lstm_score, lstm_is_anomaly, mse = lstm.predict(X_norm[:1])
    print(f"  LSTM 重建误差 (首样本): {mse:.4f}")
    print(f"  LSTM 异常得分: {lstm_score:.4f}")
    print(f"  LSTM 阈值: {lstm._threshold:.4f}")
    print("=" * 50)


if __name__ == "__main__":
    main()
