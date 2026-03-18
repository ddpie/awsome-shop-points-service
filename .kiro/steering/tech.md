# 技术栈与构建

## 运行时与语言
- Java 21
- Spring Boot 3.4.1
- Spring Cloud 2025.0.0

## 构建系统
- Maven（多模块 POM）
- Lombok 1.18.36（注解处理器）
- JaCoCo 代码覆盖率

## ORM 与数据库
- MyBatis-Plus 3.5.7（Spring Boot 3 starter）
- MySQL 8.4，使用 Druid 连接池
- Flyway 管理数据库迁移（脚本位于 `bootstrap/src/main/resources/db/migration/`）
- 迁移脚本命名：`V{number}__{description}.sql`
- 数据库名：points_db
- 数据表：point_balances、point_transactions、system_configs、distribution_batches（设计文档要求）

## 并发控制
- 悲观锁：`SELECT ... FOR UPDATE`
- 锁超时：5 秒（事务级别 `SET innodb_lock_wait_timeout = 5`）— 设计文档要求
- 适用场景：积分扣除、管理员手动调整、积分回滚
- 余额更新使用直接 SQL（绕过乐观锁），配合悲观锁使用

## 缓存
- Spring Data Redis，使用 Lettuce 客户端

## 消息队列
- AWS SQS（SDK 2.20.0）

## 安全
- JJWT 0.12.6（JWT 创建/验证）
- 认证在网关层统一处理
- 内部接口（/api/internal/*）不需要 JWT 认证

## API 文档
- SpringDoc OpenAPI（Swagger UI 访问地址：`/swagger-ui.html`）

## 定时任务
- Spring `@Scheduled` + `@EnableScheduling`
- cron 表达式：`0 0 2 1 * ?`（每月1日凌晨2:00）
- 硬编码在代码中，不可通过配置修改
- 每条发放使用独立事务（`REQUIRES_NEW`）

## 可观测性
- Micrometer Tracing 1.3.5
- Logstash Logback Encoder 7.4
- Actuator 端点：health、info、prometheus

## 常用命令

```bash
# 全量构建（跳过测试）
mvn clean install -DskipTests

# 运行测试
mvn test

# 启动应用（local 配置，端口 8003）
mvn spring-boot:run -pl bootstrap

# 使用指定配置启动
mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=dev

# 构建单个模块
mvn clean install -pl domain/domain-model -am

# 运行领域层单元测试
mvn test -pl domain/domain-impl -am -Djacoco.skip=true
```

## 环境配置
- `local`（默认）— 本地开发
- `dev` — 开发环境
- `docker` — Docker 部署
- `staging` — 预发布环境
- `prod` — 生产环境
- `test` — 测试环境

## 部署配置
- 容器名：awsomeshop-points
- 内部端口：8003
- 网络：awsomeshop-net (bridge)
- 健康检查：`GET /actuator/health`（interval 15s, timeout 5s, retries 3, start_period 30s）
- 环境变量：DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, SERVER_PORT

## 性能目标
| 指标 | 目标值 |
|------|--------|
| API P95 响应时间 | ≤ 200ms |
| 悲观锁超时 | 5 秒 |
| 单条发放耗时 | < 50ms |
| 1000 用户发放总耗时 | < 60 秒 |
