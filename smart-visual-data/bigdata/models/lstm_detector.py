"""
LSTM 自编码器时序异常检测模块
功能：训练 → 预测 → 模型持久化
用途：捕捉时序趋势突变、隐性关联异常
原理：LSTM Autoencoder 学习正常时序模式，重建误差大 = 异常概率高
"""
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers, models, optimizers

from .config import LSTM_EPOCHS, LSTM_BATCH_SIZE, LSTM_LEARNING_RATE, LSTM_HIDDEN_UNITS, LSTM_MODEL_PATH


class LSTMAutoencoder:
    def __init__(self):
        self.model: models.Model = None
        self._threshold: float = None  # 训练集重建误差的 95 分位数

    def build_model(self, n_features: int, window: int):
        """构建 LSTM Autoencoder"""
        inputs = keras.Input(shape=(window, n_features))

        # Encoder
        x = layers.LSTM(LSTM_HIDDEN_UNITS, activation="tanh", return_sequences=True)(inputs)
        x = layers.LSTM(LSTM_HIDDEN_UNITS // 2, activation="tanh", return_sequences=False)(x)
        x = layers.RepeatVector(window)(x)

        # Decoder
        x = layers.LSTM(LSTM_HIDDEN_UNITS // 2, activation="tanh", return_sequences=True)(x)
        x = layers.LSTM(LSTM_HIDDEN_UNITS, activation="tanh", return_sequences=True)(x)
        outputs = layers.TimeDistributed(layers.Dense(n_features))(x)

        self.model = models.Model(inputs, outputs)
        self.model.compile(
            optimizer=optimizers.Adam(learning_rate=LSTM_LEARNING_RATE),
            loss="mse",
        )

    def train(self, X: np.ndarray, epochs: int = LSTM_EPOCHS, batch_size: int = LSTM_BATCH_SIZE):
        """
        训练 LSTM Autoencoder
        X: shape=(样本数, window, n_features)
        """
        if self.model is None:
            raise RuntimeError("请先调用 build_model()")

        history = self.model.fit(
            X, X,
            epochs=epochs,
            batch_size=batch_size,
            validation_split=0.1,
            shuffle=True,
            verbose=1,
        )

        # 计算训练集重建误差的阈值
        reconstructed = self.model.predict(X, verbose=0)
        mse = np.mean(np.square(X - reconstructed), axis=(1, 2))
        self._threshold = float(np.percentile(mse, 95))

        return history

    def predict(self, X: np.ndarray) -> tuple:
        """
        预测异常得分。
        返回 (anomaly_score, is_anomaly, mse)
          - anomaly_score: [0, 1] 越大越异常
          - is_anomaly: bool
          - mse: 原始重建误差
        """
        if self.model is None:
            raise RuntimeError("模型未加载")

        reconstructed = self.model.predict(X, verbose=0)
        mse = float(np.mean(np.square(X - reconstructed)))

        # 将 MSE 映射到 [0,1]：超过 threshold 越多越异常
        if self._threshold and self._threshold > 0:
            score = min(mse / self._threshold, 1.0)
        else:
            score = 0.0

        is_anomaly = mse > (self._threshold or float("inf"))
        return score, is_anomaly, mse

    def save(self, path: str = LSTM_MODEL_PATH):
        self.model.save(path)
        # 同时保存阈值
        np.save(path + ".threshold.npy", np.array([self._threshold]))

    def load(self, path: str = LSTM_MODEL_PATH):
        self.model = models.load_model(path)
        try:
            self._threshold = float(np.load(path + ".threshold.npy")[0])
        except (FileNotFoundError, IndexError):
            self._threshold = None
        return self
