---
inclusion: fileMatch
fileMatchPattern: "domain/**"
---

# Domain 层编码规则

领域层是业务核心，包含领域模型、领域服务接口/实现、以及各类 Port 接口。

## 子模块职责

### domain-model
- 纯 Java POJO，不依赖 Spring 或任何框架注解
- 实体命名：`{Name}Entity`，使用 `@Data`
- 业务行为方法直接定义在实体上（充血模型），如 `addBalance()`、`deductBalance()`、`hasSufficientBalance()`
- 包路径：`domain.model.{aggregate}`
- 枚举类型（如 `TransactionType`）也放在 domain-model 中

### domain-api
- 领域服务接口，定义业务操作契约
- 命名：`{Name}DomainService`
- 包路径：`domain.service.{aggregate}`
- 方法参数使用基本类型或领域实体，不使用 DTO

### domain-impl
- 领域服务实现，使用 `@Service` + `@RequiredArgsConstructor`
- 命名：`{Name}DomainServiceImpl`
- 包路径：`domain.impl.service.{aggregate}`
- 只依赖 Port 接口（repository-api、cache-api、mq-api、security-api），绝不直接依赖基础设施实现
- 业务校验失败时抛出 `BusinessException(ErrorCode)`

#### 积分领域服务（PointDomainService）核心方法
| 方法 | 事务 | 悲观锁 | 说明 |
|------|------|--------|------|
| initBalance(userId) | @Transactional | 否 | 幂等初始化，已存在直接返回 |
| getBalance(userId) | 无 | 否 | 不存在抛 BALANCE_NOT_FOUND |
| getTransactions(userId, page, size, type) | 无 | 否 | 分页+类型筛选 |
| pageBalances(page, size, userId) | 无 | 否 | 管理员分页查询 |
| adjustPoints(userId, amount, remark, operatorId) | @Transactional | FOR UPDATE | 扣除时校验余额 |
| deductPoints(userId, amount, orderId) | @Transactional | FOR UPDATE | amount 存储为负数 |
| rollbackDeduction(transactionId) | @Transactional | FOR UPDATE | 校验原始类型+唯一性 |
| getDistributionAmount() | 无 | 否 | 默认值 100 |
| updateDistributionAmount(amount) | 无 | 否 | UPSERT 配置 |
| findAllBalances() | 无 | 否 | 定时发放用 |
| distributeToUser(userId, amount, remark) | @Transactional(REQUIRES_NEW) | FOR UPDATE | 独立事务，单条失败不影响其他 |

### Port 接口（repository-api / cache-api / mq-api / security-api）
- 定义基础设施访问契约，由 infrastructure 层实现
- Repository 接口返回领域实体（`{Name}Entity`），不暴露持久化细节
- 命名：`{Name}Repository`、`{Name}Cache`、`{Name}MessageProducer` 等
- 包路径：`repository.{aggregate}`、`cache.{aggregate}` 等

#### 积分仓储端口
- `PointBalanceRepository`：getByUserId, getByUserIdForUpdate（悲观锁）, save, updateBalance, page, findAll
- `PointTransactionRepository`：save, getById, pageByUserId（分页+类型筛选）, existsRollbackByReferenceId
- `SystemConfigRepository`：getByKey, upsert

## 禁止事项
- domain-model 中不允许出现 Spring 注解
- domain-api/domain-impl 不允许直接依赖 infrastructure 实现类
- 不允许在领域层处理 HTTP 请求/响应相关逻辑
- 不允许在领域层引用 DTO 或 Request 对象
