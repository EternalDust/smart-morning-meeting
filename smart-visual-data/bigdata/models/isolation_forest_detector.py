"""
孤立森林 (Isolation Forest) 异常检测模块
功能：训练 → 预测 → 模型持久化
用途：快速识别参会率骤降、解决率不达标等显性异常
"""
import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
import joblib

from .config import IF_CONTAMINATION, IF_N_ESTIMATORS, IF_MAX_SAMPLES, IF_MODEL_PATH


class IFDetector:
    def __init__(self):
        self.model: IsolationForest = None

    def train(self, features: np.ndarray):
        """
        训练 Isolation Forest 模型
        features: shape=(n_samples, n_features)
        """
        self.model = IsolationForest(
            n_estimators=IF_N_ESTIMATORS,
            max_samples=min(IF_MAX_SAMPLES, len(features)),
            contamination=IF_CONTAMINATION,
            random_state=42,
            n_jobs=-1,
        )
        self.model.fit(features)
        return self

    def predict(self, features: np.ndarray) -> tuple:
        """
        预测单条或多条数据。
        返回 (anomaly_score, is_anomaly)
          - anomaly_score: [0, 1] 越大越异常（做了归一化映射）
          - is_anomaly: True / False
        """
        if self.model is None:
            raise RuntimeError("模型未加载，请先调用 train() 或 load()")

        # decision_function 返回负值越低越异常
        raw_scores = self.model.decision_function(features.reshape(1, -1) if features.ndim == 1 else features)
        # 将 score 映射到 [0, 1]，-0.5→0, -0.7→0.67, 0.1→0 等
        anomaly_scores = 1 - (raw_scores + 0.5) / 1.5  # 偏移映射
        anomaly_scores = np.clip(anomaly_scores, 0, 1)

        labels = self.model.predict(features.reshape(1, -1) if features.ndim == 1 else features)
        is_anomaly = labels == -1

        return float(anomaly_scores[0]) if features.ndim == 1 else anomaly_scores, \
               bool(is_anomaly[0]) if features.ndim == 1 else is_anomaly

    def save(self, path: str = IF_MODEL_PATH):
        joblib.dump(self.model, path)

    def load(self, path: str = IF_MODEL_PATH):
        self.model = joblib.load(path)
        return self
