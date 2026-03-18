# Points Service 实现文档

## 一、架构设计

### 1.1 整体架构

采用 DDD（领域驱动设计）+ 六边形架构，以 Maven 多模块组织，严格遵循依赖倒置原则。

```
┌─────────────────────────────────────────────────────────────┐
│                      bootstrap                               │
│              Spring Boot 启动入口 / 配置 / Flyway             │
├─────────────────────────────────────────────────────────────┤
│                      interface                               │
│         ┌──────────────────┐  ┌──────────────────┐          │
│         │  interface-http  │  │interface-consumer │          │
│         │  REST Controller │  │  SQS 消费者       │          │
│         └────────┬─────────┘  └──────────────────┘          │
├──────────────────┼──────────────────────────────────────────┤
│                  ▼ application                               │
│         ┌──────────────────┐  ┌──────────────────┐          │
│         │ application-api  │  │ application-impl  │          │
│         │ 接口 + DTO       │  │ 应用服务实现       │          │
│         └──────────────────┘  └────────┬─────────┘          │
├────────────────────────────────────────┼────────────────────┤
│                  ▼ domain                                    │
│  ┌────────────┐ ┌──────────┐ ┌──────────┐ ┌─────────────┐  │
│  │domain-model│ │domain-api│ │domain-impl│ │repository-api│ │
│  │ 领域实体    │ │领域服务接口│ │领域服务实现│ │ 仓储端口     │  │
│  └────────────┘ └──────────┘ └──────────┘ └─────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                  infrastructure                              │
│         ┌──────────────────────────────────┐                │
│         │  repository / mysql-impl          │                │
│         │  PO + Mapper + Repository 实现    │                │
│         └──────────────────────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 分层依赖规则

| 层级 | 允许依赖 | 禁止依赖 |
|------|---------|---------|
| interface-http | application-api | application-impl, domain, infrastructure |
| application-impl | domain-api | repository-api, infrastructure |
| domain-impl | repository-api, cache-api, mq-api | infrastructure 具体实现 |
| mysql-impl | repository-api, domain-model | domain-impl, application |
| bootstrap | 聚合所有 impl 模块 | — |

### 1.3 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.4.1 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.4 |
| 连接池 | Druid | 1.2.20 |
| 数据库迁移 | Flyway | — |
| API 文档 | SpringDoc OpenAPI | 2.7.0 |
| 构建 | Maven 多模块 | — |
| 代码生成 | Lombok | 1.18.38 |

### 1.4 包命名规范

基础包名：`com.awsome.shop.point`

| 层级 | 包名 |
|------|------|
| 领域实体 | `domain.model.point` |
| 领域服务接口 | `domain.service.point` |
| 领域服务实现 | `domain.impl.service.point` |
| 仓储端口 | `repository.point` |
| 仓储实现 | `repository.mysql.impl.point` |
| PO | `repository.mysql.po.point` |
| Mapper | `repository.mysql.mapper.point` |
| 应用服务接口 | `application.api.service.point` |
| 请求 DTO | `application.api.dto.point.request` |
| 响应 DTO | `application.api.dto.point` |
| HTTP 控制器 | `facade.http.controller` |
| 异常处理器 | `facade.http.exception` |
| 定时任务调度器 | `application.impl.scheduler` |

---

## 二、数据库设计

### 2.1 point_balances（积分余额表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT UK | 用户ID |
| balance | INT | 当前积分余额（≥ 0） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| created_by | BIGINT | 创建人 |
| updated_by | BIGINT | 更新人 |
| deleted | TINYINT | 逻辑删除 |
| version | INT | 乐观锁 |

### 2.2 point_transactions（积分变动流水表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 流水ID |
| user_id | BIGINT | 用户ID |
| type | VARCHAR(20) | 变动类型 |
| amount | INT | 变动数量（正增负减） |
| balance_after | INT | 变动后余额 |
| reference_id | BIGINT | 关联订单ID |
| operator_id | BIGINT | 操作人ID |
| remark | VARCHAR(500) | 备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| created_by | BIGINT | 创建人 |
| updated_by | BIGINT | 更新人 |
| deleted | TINYINT | 逻辑删除 |
| version | INT | 乐观锁 |

索引：`idx_user_id`、`idx_reference_id`、`idx_type`、`idx_created_at`

### 2.3 system_configs（系统配置表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 配置ID |
| config_key | VARCHAR(100) UK | 配置键 |
| config_value | VARCHAR(500) | 配置值 |
| description | VARCHAR(500) | 配置说明 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| created_by | BIGINT | 创建人 |
| updated_by | BIGINT | 更新人 |
| deleted | TINYINT | 逻辑删除 |
| version | INT | 乐观锁 |

预置数据：`points.distribution.amount = 100`（每月发放额度）

### 2.4 变动类型枚举

| 值 | 说明 | amount 符号 |
|----|------|-------------|
| DISTRIBUTION | 系统自动发放 | 正 |
| MANUAL_ADD | 管理员手动增加 | 正 |
| MANUAL_DEDUCT | 管理员手动扣除 | 负 |
| REDEMPTION | 兑换扣除 | 负 |
| ROLLBACK | 兑换回滚 | 正 |

### 2.5 设计文档要求但尚未实现的表

#### distribution_batches（发放批次表）

设计文档（nfr-design/nfr-design-patterns.md、nfr-requirements/tech-stack-decisions.md）要求新增此表，用于记录每次定时发放的执行状态，支持补发。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 批次ID |
| distribution_amount | INT | 本次发放额度 |
| total_count | INT | 应发放总人数 |
| success_count | INT | 成功发放人数 |
| fail_count | INT | 失败人数 |
| status | ENUM('RUNNING','COMPLETED','FAILED') | 批次状态 |
| started_at | DATETIME | 开始时间 |
| completed_at | DATETIME | 完成时间 |

---

## 三、API 接口

API 路径以设计文档（aidlc-docs/construction/points-service）定义为准，使用 `/api/v1/` 版本前缀。

### 3.1 员工端点（/api/v1/point）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/point/balance | 查询当前用户积分余额（X-User-Id 请求头） |
| GET | /api/v1/point/transactions?page=&size=&type= | 查询当前用户积分变动历史 |

### 3.2 管理员端点（/api/v1/point/admin）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/point/admin/balances?page=&size=&keyword= | 查看所有员工积分余额 |
| GET | /api/v1/point/admin/transactions/{userId}?page=&size=&type= | 查看指定员工积分变动明细 |
| POST | /api/v1/point/admin/adjust | 手动调整员工积分 |
| GET | /api/v1/point/admin/config | 获取发放配置 |
| PUT | /api/v1/point/admin/config | 更新发放配置 |

### 3.3 内部端点（/api/v1/internal/point，服务间调用）

| 方法 | 路径 | 调用方 |
|------|------|--------|
| POST | /api/v1/internal/point/init | auth-service |
| POST | /api/v1/internal/point/deduct | order-service |
| POST | /api/v1/internal/point/rollback | order-service |
| GET | /api/v1/internal/point/balance/{userId} | order-service |

### 3.4 分页参数

| 参数 | 默认值 | 范围 | 说明 |
|------|--------|------|------|
| page | 0 | ≥ 0 | 页码从 0 开始，Application 层 +1 转换适配 MyBatis-Plus |
| size | 20 | 1 ~ 100 | 每页大小 |

---

## 四、核心业务逻辑

### 4.1 积分初始化（幂等）— BR-002

- auth-service 用户注册成功后调用
- 如果 userId 已存在余额记录，直接返回（幂等）
- 初始余额为 0
- 失败不影响 auth-service 注册流程

### 4.2 管理员手动调整 — BR-001/BR-004/BR-007

- 悲观锁（SELECT FOR UPDATE）锁定余额行
- amount > 0 增加（type=MANUAL_ADD），amount < 0 扣除（type=MANUAL_DEDUCT）
- 扣除时校验余额充足（balance + amount ≥ 0）
- 余额更新和流水创建在同一事务中（BR-003）
- operatorId 记录操作人（审计），remark 必填（BR-007）

### 4.3 兑换扣除 — BR-001/BR-004/BR-010

- 悲观锁锁定余额行
- 校验余额 ≥ 扣除数量
- 流水 amount 存储为负数（BR-010: REDEMPTION amount < 0）
- referenceId 关联订单ID
- remark 固定为 "兑换扣除"

### 4.4 兑换回滚 — BR-005/BR-006

- 校验原始记录存在且类型为 REDEMPTION（BR-006）
- 校验未被重复回滚：查询是否存在同 referenceId 的 ROLLBACK 记录（BR-005）
- 悲观锁恢复余额
- 回滚记录 referenceId 关联原始订单ID，remark 固定为 "兑换回滚"
- 步骤 2-5 在同一事务中

### 4.5 定时发放 — BR-004/BR-008/BR-009

- cron: `0 0 2 1 * ?`（每月1日凌晨2:00）
- 从 system_configs 读取发放额度，不存在时使用默认值 100（BR-009）
- 遍历 point_balances 所有记录，逐条发放
- 使用原子 UPDATE（`addBalanceAtomic`）而非悲观锁（BR-004: 定时发放不使用 SELECT FOR UPDATE）
- 每条发放使用独立事务（REQUIRES_NEW），单条失败不影响其他用户（BR-008）
- 发放备注格式：`系统自动发放 - YYYY年MM月`
- 日志记录发放结果（总人数、成功数、失败数）

---

## 五、错误码

| 枚举值 | 错误码 | HTTP 状态码 | 消息 | 设计文档对应 |
|--------|--------|-----------|------|-------------|
| BALANCE_NOT_FOUND | NOT_FOUND_001 | 404 | 积分余额记录不存在 | POINTS_001 |
| INSUFFICIENT_BALANCE_ADJUST | BAD_REQUEST_001 | 400 | 扣除后余额不足 | POINTS_002 |
| INSUFFICIENT_BALANCE_REDEEM | BAD_REQUEST_002 | 400 | 积分不足，无法兑换 | POINTS_003 |
| TRANSACTION_NOT_FOUND | NOT_FOUND_002 | 404 | 积分变动记录不存在 | POINTS_004 |
| INVALID_ROLLBACK_TYPE | BAD_REQUEST_003 | 400 | 只能回滚兑换扣除记录 | POINTS_005 |
| DUPLICATE_ROLLBACK | CONFLICT_001 | 409 | 该笔扣除已回滚，不可重复操作 | POINTS_006 |

错误码格式：`{CATEGORY}_{SEQ}`，由 GlobalExceptionHandler 根据前缀自动映射 HTTP 状态码。

---

## 六、并发控制

| 操作 | 并发策略 | 说明 |
|------|---------|------|
| 兑换扣除 | 悲观锁 SELECT FOR UPDATE | 防止并发扣除导致余额为负 |
| 管理员调整 | 悲观锁 SELECT FOR UPDATE | 防止并发调整数据不一致 |
| 积分回滚 | 悲观锁 SELECT FOR UPDATE | 防止并发回滚 |
| 定时发放 | 原子 UPDATE（addBalanceAtomic） | 不使用悲观锁，避免与兑换扣除冲突 |

- PointBalanceMapper 提供 `selectByUserIdForUpdate`（悲观锁）和 `addBalanceByUserId`（原子更新）
- 余额更新使用直接 SQL（`updateBalanceByUserId`），绕过乐观锁，配合悲观锁使用
- 设计文档要求事务级别设置 `SET innodb_lock_wait_timeout = 5`（尚未实现）

---

## 七、单元测试

测试类：`PointDomainServiceImplTest`（34 个测试用例）

| 测试组 | 用例数 | 覆盖场景 | 关联业务规则 |
|--------|--------|---------|-------------|
| initBalance | 2 | 幂等返回 / 新建初始化 | BR-002 |
| getBalance | 2 | 正常返回 / 不存在抛异常 | NOT_FOUND_001 |
| getTransactions | 2 | 分页查询 / 按类型筛选 | — |
| pageBalances | 2 | 分页查询所有 / 按 userId 精确匹配 | — |
| adjustPoints | 6 | 增加(MANUAL_ADD) / 扣除(MANUAL_DEDUCT) / 扣除到0边界 / 余额不足 / 记录不存在 / operatorId+remark审计 | BR-001, BR-004, BR-007, BR-010 |
| deductPoints | 5 | 成功(amount负数) / 余额不足 / 记录不存在 / 扣到0 / balanceAfter一致性 | BR-001, BR-003, BR-010 |
| rollbackDeduction | 7 | 成功(ROLLBACK) / 记录不存在 / MANUAL_DEDUCT不可回滚 / DISTRIBUTION不可回滚 / MANUAL_ADD不可回滚 / ROLLBACK不可回滚 / 重复回滚 | BR-005, BR-006, BR-010 |
| getDistributionAmount | 2 | 配置存在 / 默认值100 | BR-009 |
| updateDistributionAmount | 1 | 更新配置(key+value+description) | — |
| findAllBalances | 2 | 返回全部 / 空列表 | — |
| distributeToUser | 3 | 发放成功(原子UPDATE) / 余额不存在跳过 / 不使用悲观锁验证 | BR-003, BR-004, BR-008, BR-010 |

### 测试覆盖的设计文档业务规则

| 业务规则 | 说明 | 测试覆盖 |
|---------|------|---------|
| BR-001 | 积分余额非负 | adjustPoints(余额不足/扣到0), deductPoints(余额不足/扣到0) |
| BR-002 | 积分初始化幂等 | initBalance(幂等返回/新建) |
| BR-003 | 积分变动必须记录流水 | deductPoints(balanceAfter一致性), distributeToUser(同时创建) |
| BR-004 | 并发控制策略 | adjustPoints/deductPoints/rollbackDeduction使用悲观锁, distributeToUser使用原子UPDATE |
| BR-005 | 回滚唯一性 | rollbackDeduction(重复回滚) |
| BR-006 | 回滚类型限制 | rollbackDeduction(4种非REDEMPTION类型) |
| BR-007 | 手动调整必须填写备注 | adjustPoints(operatorId+remark审计) |
| BR-008 | 自动发放独立事务 | distributeToUser(REQUIRES_NEW) |
| BR-009 | 发放配置默认值 | getDistributionAmount(默认值100) |
| BR-010 | 变动类型与amount符号一致性 | adjustPoints/deductPoints/rollbackDeduction/distributeToUser |

---

## 八、文件清单

```
common/
  └── annotation/RequireOwnerPermission
  └── dto/PageResult
  └── enums/ErrorCode, ParamErrorCode, PointsErrorCode, SampleErrorCode, SystemErrorCode
  └── exception/BaseException, BusinessException, ParameterException, SystemException
  └── result/Result

domain/domain-model/
  └── model/point/PointBalanceEntity, PointTransactionEntity, SystemConfigEntity, TransactionType
  └── model/test/TestEntity

domain/repository-api/
  └── repository/point/PointBalanceRepository, PointTransactionRepository, SystemConfigRepository
  └── repository/test/TestRepository

domain/domain-api/
  └── service/point/PointDomainService
  └── service/test/TestDomainService

domain/domain-impl/
  └── impl/service/point/PointDomainServiceImpl
  └── impl/service/test/TestDomainServiceImpl
  └── [test] impl/service/point/PointDomainServiceImplTest (34 用例)

domain/security-api/
  └── infrastructure/security/api/service/EncryptionService

application/application-api/
  └── dto/point/PointBalanceDTO, PointTransactionDTO, DistributionConfigDTO
  └── dto/point/request/InitPointsRequest, AdjustPointsRequest, DeductPointsRequest,
      RollbackDeductionRequest, QueryBalancesRequest, QueryTransactionsRequest,
      UpdateDistributionConfigRequest
  └── dto/test/TestDTO
  └── dto/test/request/CreateTestRequest, UpdateTestRequest, GetTestRequest,
      ListTestRequest, DeleteTestRequest
  └── service/point/PointApplicationService
  └── service/test/TestApplicationService

application/application-impl/
  └── service/point/PointApplicationServiceImpl
  └── service/test/TestApplicationServiceImpl
  └── scheduler/PointDistributionScheduler

infrastructure/repository/mysql-impl/
  └── po/point/PointBalancePO, PointTransactionPO, SystemConfigPO
  └── po/test/TestPO
  └── mapper/point/PointBalanceMapper, PointTransactionMapper, SystemConfigMapper
  └── mapper/test/TestMapper
  └── impl/point/PointBalanceRepositoryImpl, PointTransactionRepositoryImpl,
      SystemConfigRepositoryImpl
  └── impl/test/TestRepositoryImpl
  └── config/MybatisPlusConfig, CustomMetaObjectHandler, UserContext

infrastructure/cache/redis-impl/
  └── config/RedisConfig

infrastructure/security/jwt-impl/
  └── security/crypto/AesEncryptionServiceImpl

interface/interface-http/
  └── controller/PointController, PointAdminController, PointInternalController, TestController
  └── exception/GlobalExceptionHandler
  └── request/common/GatewayInjectableRequest, PageableRequest
  └── response/Result, ErrorDetail

interface/interface-consumer/
  └── exception/GlobalConsumerExceptionHandler

bootstrap/
  └── Application (@EnableScheduling)
  └── config/OpenApiConfig, AsyncConfig
  └── resources/db/migration/V1__create_points_tables.sql
```

---

## 九、设计文档要求但尚未实现的功能

以下功能在设计文档（aidlc-docs/construction/points-service）中定义但当前代码尚未实现：

| # | 功能 | 设计文档来源 | 说明 |
|---|------|-------------|------|
| 1 | distribution_batches 表 | tech-stack-decisions.md | 发放批次记录表，记录每次发放执行状态 |
| 2 | DistributionBatchRepository | logical-components.md | 发放批次数据访问层 |
| 3 | 补发逻辑 | nfr-design-patterns.md | 服务重启后检查 RUNNING 状态批次并补发 |
| 4 | 悲观锁超时设置 | nfr-design-patterns.md | 事务级别 `SET innodb_lock_wait_timeout = 5` |
| 5 | DistributionConfigDTO.updatedAt | domain-entities.md | DistributionConfigResponse 应包含 updatedAt 字段 |

---

## 十、本地运行

```bash
# 创建数据库
mysql -h 127.0.0.1 -u root -p -e "CREATE DATABASE awsome_shop_point DEFAULT CHARSET utf8mb4;"

# 编译安装
mvn clean install -DskipTests -Djacoco.skip=true

# 启动服务（端口 8003）
mvn spring-boot:run -pl bootstrap -DskipTests -Djacoco.skip=true

# Swagger UI
open http://localhost:8003/swagger-ui.html

# 运行单元测试（34 用例）
mvn test -pl domain/domain-impl -am -Djacoco.skip=true
```
