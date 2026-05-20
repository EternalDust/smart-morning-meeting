"""
特征工程模块
  - extract_features: 单条原始记录 → 特征向量
  - build_sequences:  历史 DataFrame → LSTM 时序窗口序列
  - normalize / inverse_normalize
"""
import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler
import joblib

from .config import FEATURE_COLUMNS, SEQUENCE_WINDOW, SCALER_PATH


def extract_features(row: dict) -> np.ndarray:
    """
    从单条数据字典中提取特征向量。
    row 预期包含: attend_rate, solve_rate, late_num, absent_num, ...
    返回 shape=(n_features,) 的 numpy 数组
    """
    vec = []
    for col in FEATURE_COLUMNS:
        val = row.get(col, 0.0)
        if val is None:
            val = 0.0
        vec.append(float(val))
    return np.array(vec, dtype=np.float32)


def build_sequences(df: pd.DataFrame, window: int = SEQUENCE_WINDOW):
    """
    从历史 DataFrame 构建 LSTM 所需的时间窗口序列。
    df 需按 stat_date 升序排列。

    返回:
      X: shape=(样本数, window, n_features)
      y: shape=(样本数, n_features)   — 窗口下一时刻的真实值
    """
    if len(df) <= window:
        raise ValueError(f"数据量 ({len(df)}) 不足以构成窗口 ({window})")

    features = df[FEATURE_COLUMNS].values.astype(np.float32)
    X, y = [], []
    for i in range(len(features) - window):
        X.append(features[i : i + window])
        y.append(features[i + window])
    return np.array(X), np.array(y)


def fit_scaler(df: pd.DataFrame) -> StandardScaler:
    """在训练集上拟合 StandardScaler 并保存"""
    scaler = StandardScaler()
    scaler.fit(df[FEATURE_COLUMNS].values)
    joblib.dump(scaler, SCALER_PATH)
    return scaler


def load_scaler() -> StandardScaler:
    return joblib.load(SCALER_PATH)


def normalize(scaler: StandardScaler, features: np.ndarray) -> np.ndarray:
    """标准化"""
    if features.ndim == 1:
        return scaler.transform(features.reshape(1, -1)).flatten()
    return scaler.transform(features)


def inverse_normalize(scaler: StandardScaler, features: np.ndarray) -> np.ndarray:
    """反标准化"""
    if features.ndim == 1:
        return scaler.inverse_transform(features.reshape(1, -1)).flatten()
    return scaler.inverse_transform(features)
