"""
全局配置：特征列、LSTM窗口、风险阈值、模型路径、数据库连接等
"""

# ── 特征工程 ──
FEATURE_COLUMNS = [
    "attend_rate",       # 参会率 (%)
    "solve_rate",        # 问题解决率 (%)
    "late_num",          # 迟到人数
    "absent_num",        # 缺席人数
    "overdue_count",     # 逾期督办数
    "urgent_count",      # 紧急问题数
    "avg_duration",      # 晨会平均时长 (分钟)
]

SEQUENCE_WINDOW = 3     # LSTM 输入时间窗口（连续天数，mock数据仅7天故设为3）

# ── 孤立森林 ──
IF_CONTAMINATION = 0.1      # 预期异常比例
IF_N_ESTIMATORS = 200        # 树的数量
IF_MAX_SAMPLES = 256         # 每棵树采样数

# ── LSTM ──
LSTM_EPOCHS = 3
LSTM_BATCH_SIZE = 16
LSTM_LEARNING_RATE = 0.001
LSTM_HIDDEN_UNITS = 32

# ── 风险融合（三级风险） ──
# 融合权重：LSTM 捕捉趋势能力更强，权重更高
IF_WEIGHT = 0.4
LSTM_WEIGHT = 0.6

RISK_THRESHOLDS = {
    "level_1": {"name": "一般预警", "min_score": 0.50, "max_score": 0.70},
    "level_2": {"name": "中等预警", "min_score": 0.70, "max_score": 0.90},
    "level_3": {"name": "重大预警", "min_score": 0.90, "max_score": 1.01},
}

# ── 预警类型 ──
WARN_TYPES = {
    "ATTEND_DROP": "参会率骤降",
    "SOLVE_DROP": "问题解决率不达标",
    "OVERDUE_SURGE": "逾期数激增",
    "DURATION_ABNORMAL": "晨会时长异常",
    "COMPLETE_DROP": "业务指标完成度下降",
    "ABSENT_SURGE": "缺席人数异常增多",
}

# ── 模型持久化 ──
MODEL_SAVE_DIR = "saved_models/"
IF_MODEL_PATH = MODEL_SAVE_DIR + "isolation_forest.pkl"
LSTM_MODEL_PATH = MODEL_SAVE_DIR + "lstm_autoencoder.keras"
SCALER_PATH = MODEL_SAVE_DIR + "scaler.pkl"

# ── 数据库（MySQL — 写入 bi_warn_record 表） ──
MYSQL_HOST = "localhost"
MYSQL_PORT = 3306
MYSQL_DB = "smm_db"
MYSQL_USER = "root"
MYSQL_PASSWORD = "hqh123"

# ── Redis ──
REDIS_HOST = "localhost"
REDIS_PORT = 6379
REDIS_DB = 0
REDIS_WARN_KEY = "warn:realtime"
REDIS_MAX_CACHED = 50       # Redis 中缓存的最新预警条数
