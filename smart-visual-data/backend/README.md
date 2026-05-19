# JavaEE (Spring Boot) 后端工程说明

根据设计文档《数字医疗智慧晨会协同与决策支撑平台》，本工程负责实现：
1. **统一数据出口与可视化数据支撑**
2. **多角色/细粒度权限管控**（集成 `Spring Security`）
3. **高并发缓存查询支持**（集成 `Redis`，承接由于大屏并发访问带来的压力）
4. **实时数据大屏长连接**（集成 `WebSocket`）

## 工程结构
- `/config/SecurityConfig.java`: 实现了文档要求的高、中、基层人员的三级精细化权限分层。
- `/config/RedisConfig.java`: 配置了热点数据的缓存模板。
- `/config/WebSocketConfig.java`: 配置WebSocket以打通长连接，实现文档中所诉的“数据推模式更新”。
- `/websocket/MorningMeetingDataHandler.java`: 专门处理多大屏的WebSocket长连接并发通信。
- `/service/DashboardDataService.java`: 模拟了从Redis（或Spark处理端）获取数据后向大屏实时推流的逻辑。
- `/controller/DashboardController.java`: 定义了按层级隔离的数据获取API。

## 下一步建议
由于系统是一个大数据系统，后端写好骨架后，我们的数据来源实际上由大数据端处理。所以接下来我们可以进行 **B（Spark/Kafka）** 或 **C（孤立森林/LSTM算法模型）** 模块的开发，将后端“等米下锅”的数据缺口补齐。
