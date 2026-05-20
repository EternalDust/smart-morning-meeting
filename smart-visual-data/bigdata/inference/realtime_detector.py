"""
实时检测主逻辑
供 Spark Streaming foreachBatch 调用
流程: 特征提取 → IF推理 → LSTM推理 → 风险融合 → 结果写入
"""
import numpy as np
from typing import Optional

from models.config import FEATURE_COLUMNS, SEQUENCE_WINDOW
from models.feature_engineering import extract_features, normalize, load_scaler
from models.isolation_forest_detector import IFDetector
from models.lstm_detector import LSTMAutoencoder
from models.risk_fusion import fuse
from inference.result_writer import ResultWriter


class RealtimeDetector:
    """单例式检测器，在 Spark Streaming 初始化时加载一次"""

    def __init__(self):
        self.if_detector = IFDetector()
        self.lstm_detector = LSTMAutoencoder()
        self.writer = ResultWriter()
        self.scaler = None
        self._history_buffer = []  # 维护最近 N 条特征用于 LSTM 窗口构建

        self._loaded = False

    def load_models(self):
        """加载已训练的模型和 scaler"""
        try:
            self.if_detector.load()
            self.lstm_detector.load()
            self.scaler = load_scaler()
            self._loaded = True
        except FileNotFoundError as e:
            print(f"[WARN] 模型文件未找到，请先运行训练脚本: {e}")
            self._loaded = False

    @property
    def is_ready(self) -> bool:
        return self._loaded

    def get_lstm_window(self) -> Optional[np.ndarray]:
        """
        从历史缓冲中构建 LSTM 所需的时间窗口。
        返回 shape=(1, window, n_features) 或 None（样本不足时）
        """
        if len(self._history_buffer) < SEQUENCE_WINDOW:
            return None
        window = np.array(self._history_buffer[-SEQUENCE_WINDOW:], dtype=np.float32)
        return window.reshape(1, SEQUENCE_WINDOW, -1)

    def detect(self, raw_data: dict) -> Optional[dict]:
        """
        对单条数据执行异常检测。
        raw_data: Kafka 消费的一条原始数据。

        返回预警记录字典，或 None（无异常）。
        """
        if not self._loaded:
            return None

        # 1. 特征提取
        features_vec = extract_features(raw_data)
        features_norm = normalize(self.scaler, features_vec)

        # 2. 构造特征字典（用于风险融合中的可读判断）
        features_dict = {col: raw_data.get(col, 0.0) for col in FEATURE_COLUMNS}

        # 3. 更新历史缓冲
        self._history_buffer.append(features_vec)

        # 4. IF 检测
        if_score, if_anomaly = self.if_detector.predict(features_norm)

        # 5. LSTM 检测（需要足够的历史窗口）
        lstm_score, lstm_anomaly = 0.0, False
        lstm_window = self.get_lstm_window()
        if lstm_window is not None:
            lstm_window_norm = normalize(self.scaler, lstm_window[0])
            lstm_window_norm = lstm_window_norm.reshape(1, SEQUENCE_WINDOW, -1)
            lstm_score, lstm_anomaly, _ = self.lstm_detector.predict(lstm_window_norm)

        # 6. 风险融合
        record = fuse(
            if_score=if_score,
            lstm_score=lstm_score,
            if_is_anomaly=if_anomaly,
            lstm_is_anomaly=lstm_anomaly,
            features=features_dict,
            raw_data=raw_data,
        )

        if record:
            # 7. 写入 MySQL + Redis
            self.writer.write(record)

        return record
