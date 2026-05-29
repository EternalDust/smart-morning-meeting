# 大数据分析与可视化决策子系统

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + Vite + ECharts 5 + Axios |
| 后端 | Spring Boot 3.2.5 + Spring Security + MyBatis-Plus + WebSocket + Redis |
| 大数据 | Apache Kafka + Spark Structured Streaming + scikit-learn + TensorFlow LSTM |
| 数据库 | MySQL 8.0 (smart_meeting) |

## 功能总览

整个子系统覆盖"数据采集 → 实时处理 → 算法检测 → 可视化展示"完整链路：

1. **多角色大屏展示** — 高层/中层/基层三种角色，数据按权限隔离
2. **实时数据推流** — WebSocket 长连接，后端主动推送指标到大屏
3. **趋势分析** — 近7天参会率趋势（ECharts 折线图）
4. **异常分布** — 各部门问题督办分布（ECharts 饼图）
5. **实时告警** — 异常检测结果实时推送，颜色分级展示（紧急/严重/一般）
6. **Kafka 数据模拟** — 模拟各部门晨会数据，持续写入 Kafka
7. **Spark Streaming 实时聚合** — 消费 Kafka，聚合指标写入 Redis
8. **异常检测双模型** — Isolation Forest + LSTM Autoencoder 融合判定
9. **模型训练流水线** — 从 MySQL 拉取历史数据，训练并保存模型

## 项目结构

```
smart-visual-data/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/huadi/smm/
│   │   ├── controller/             # API 控制器
│   │   │   ├── AuthController.java           # 登录认证
│   │   │   ├── DashboardController.java      # 大屏数据接口
│   │   │   └── AnomalyWarningController.java # 告警记录接口
│   │   ├── service/                # 业务逻辑
│   │   │   ├── DashboardDataService.java     # 大屏数据聚合 + WebSocket 推流
│   │   │   └── impl/               # 各实体服务实现
│   │   ├── entity/                 # 数据库实体
│   │   ├── mapper/                 # MyBatis-Plus 数据访问
│   │   ├── config/                 # 配置类
│   │   │   ├── SecurityConfig.java          # Spring Security 三级权限
│   │   │   ├── JwtInterceptor.java          # JWT 鉴权拦截器
│   │   │   ├── WebSocketConfig.java         # WebSocket 端点配置
│   │   │   └── RedisConfig.java             # Redis 缓存配置
│   │   ├── websocket/
│   │   │   └── MorningMeetingDataHandler.java # WebSocket 连接管理 + 消息广播
│   │   └── common/
│   │       └── ApiResponse.java             # 统一响应格式
│   ├── sql/
│   │   ├── schema.sql              # 建表语句（7张业务表）
│   │   └── sys_user.sql            # 用户表 + 种子数据
│   └── pom.xml
│
├── frontend/                       # Vue 3 前端
│   ├── src/
│   │   ├── views/
│   │   │   ├── Login.vue          # 登录页
│   │   │   └── Dashboard.vue      # 大屏主页（ECharts 图表 + 实时数据）
│   │   ├── router/index.js        # 路由（登录守卫）
│   │   ├── App.vue                # 根组件
│   │   └── main.js                # 入口
│   ├── vite.config.js
│   └── package.json
│
├── bigdata/                        # Python 大数据模块
│   ├── kafka_ingestion/
│   │   └── kafka_producer.py      # 模拟数据生产者（每2秒发送一条）
│   ├── spark_processing/
│   │   ├── spark_streaming_realtime.py   # Spark Streaming 实时处理
│   │   └── spark_batch_offline.py        # Spark SQL 离线 ETL
│   ├── models/
│   │   ├── config.py              # 全局配置
│   │   ├── feature_engineering.py # 特征工程（标准化、序列构建）
│   │   ├── isolation_forest_detector.py  # 孤立森林异常检测
│   │   ├── lstm_detector.py       # LSTM 自编码器异常检测
│   │   └── risk_fusion.py         # 双模型风险融合 + 告警分级
│   ├── inference/
│   │   ├── realtime_detector.py   # 实时检测入口（单例）
│   │   └── result_writer.py       # 结果写入 MySQL + Redis
│   ├── training/
│   │   └── train_pipeline.py      # 全流程训练流水线
│   └── saved_models/              # 预训练模型文件
│       ├── isolation_forest.pkl
│       ├── lstm_autoencoder.keras
│       └── scaler.pkl
│
└── mock_data_7days.sql            # 7天模拟晨会数据（含部门督办数据）
```

## API 接口

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录，返回 JWT Token + 用户信息 |

### 大屏数据
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/dashboard/trend` | 近7天参会率趋势 | 按 role 过滤部门 |
| GET | `/api/dashboard/issues-distribution` | 各部门问题分布 | 按 role 过滤部门 |
| GET | `/api/dashboard/base-level/data` | 近期晨会记录 | 全员 |
| POST | `/api/dashboard/test-insert` | 插入测试数据 | 全员 |

### 告警管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/warn/list` | 分页查询告警（支持按级别/类型筛选） |
| GET | `/api/warn/latest?limit=N` | 最新 N 条告警 |
| PUT | `/api/warn/handle/{id}` | 标记告警已处理 |

### WebSocket
| 端点 | 说明 |
|------|------|
| `ws://localhost:8080/ws/morning-meeting` | 实时数据推送 |

## 用户角色

| 用户名 | 密码 | 角色 | 可见数据范围 |
|--------|------|------|------------|
| admin | hqh123 | 高层 (role_id=1) | 全院汇总 |
| ken101 | hqh123 | 中层 (role_id=2) | 心内科 |
| staff101 | hqh123 | 基层 (role_id=3) | 心内科 |

## 数据库表

### 业务表（`smart_meeting`）

| 表名 | 说明 |
|------|------|
| `sys_user` | 系统用户（含角色、部门） |
| `bi_stat_meeting` | 晨会统计（参会率、缺勤等） |
| `bi_stat_supervise` | 督办统计（解决率、逾期等） |
| `bi_stat_medical` | 医疗指标统计 |
| `bi_warn_record` | 异常告警记录 |
| `bi_chart_config` | 大屏图表配置 |
| `bi_dim_dict` | 数据字典 |
| `sm_operation_log` | 操作日志表 |

## 从零启动

### 前置环境

- JDK 17+
- Node.js 16+
- MySQL 8.0
- Redis
- Kafka（大数据模块需要）
- Python 3.10+（大数据模块需要）

### 1. 初始化数据库

```sql
source backend/sql/schema.sql;
source backend/sql/sys_user.sql;
source mock_data_7days.sql;   -- 插入7天模拟数据
```

### 2. 启动后端

```bash
cd backend
# 确保 application.yml 里的数据库/Redis 连接信息正确
./mvnw spring-boot:run
# 后端运行在 http://localhost:8080
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
# 打开浏览器访问 http://localhost:5173（或 Vite 提示的地址）
```

### 4. 启动大数据流水线（可选）

按顺序启动：

```bash
# 4.1 启动 Kafka（确保已安装并启动 Zookeeper + Kafka）
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
bin/kafka-topics.sh --create --topic morning_meeting_topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# 4.2 训练模型（首次运行）
cd bigdata
pip install -r requirements.txt
python training/train_pipeline.py

# 4.3 启动数据生产者
python kafka_ingestion/kafka_producer.py

# 4.4 启动 Spark Streaming 处理（会在聚合后写入 Redis 并通过 WebSocket 推送）
python spark_processing/spark_streaming_realtime.py
```

启动后即可在大屏看到实时更新的数据。

## 异常检测模型说明

采用双模型融合策略，对晨会指标进行实时异常检测：

1. **Isolation Forest** — 基于树的集成方法，快速隔离异常点，适合高维数据
2. **LSTM Autoencoder** — 时序自编码器，捕捉时间序列上的异常模式（如参会率连续下降）
3. **风险融合** — IF 权重 0.4 + LSTM 权重 0.6，综合评分后分三级告警（一般/严重/紧急）

告警触发条件示例：
- 参会率 < 80%
- 问题解决率 < 60%
- 逾期数量激增
