---
inclusion: fileMatch
fileMatchPattern: "application/**"
---

# Application 层编码规则

应用层编排领域服务，处理用例流程，是 interface 层与 domain 层之间的桥梁。

## 子模块

### application-api
- 应用服务接口 + DTO + Request 对象
- 服务接口命名：`{Name}ApplicationService`
- 包路径：`application.api.service.{aggregate}`

#### DTO
- 命名：`{Name}DTO`，使用 `@Data`
- 包路径：`application.api.dto.{aggregate}`
- 只包含需要对外暴露的字段，不暴露内部领域细节
- 积分服务 DTO：
  - `PointBalanceDTO`：userId, balance
  - `PointTransactionDTO`：id, userId, type, amount, balanceAfter, referenceId, operatorId, remark, createdAt
  - `DistributionConfigDTO`：amount（注意：设计文档要求包含 updatedAt 字段）

#### Request 对象
- 包路径：`application.api.dto.{aggregate}.request`
- 使用 Jakarta Validation 注解校验：`@NotBlank`、`@NotNull`、`@Size`、`@Min`、`@Max` 等
- 校验消息使用中文
- 积分服务 Request：
  - `InitPointsRequest`：userId(@NotNull @Min(1))
  - `AdjustPointsRequest`：userId(@NotNull @Min(1)), amount(@NotNull), remark(@NotBlank @Size(max=500))
  - `DeductPointsRequest`：userId(@NotNull @Min(1)), amount(@NotNull @Min(1)), orderId(@NotNull @Min(1))
  - `RollbackDeductionRequest`：transactionId(@NotNull @Min(1))
  - `UpdateDistributionConfigRequest`：amount(@NotNull @Min(1))
  - `QueryBalancesRequest`：page(默认1), size(默认20, 1~100), keyword(Long, userId 精确匹配)
  - `QueryTransactionsRequest`：page(默认1), size(默认20, 1~100), type(String, 可选)

### application-impl
- 应用服务实现，使用 `@Service` + `@RequiredArgsConstructor`
- 命名：`{Name}ApplicationServiceImpl`
- 包路径：`application.impl.service.{aggregate}`
- 只依赖 Domain Service 接口（`domain-api`），绝不直接依赖 Repository 或 Infrastructure
- 在 impl 中手动编写 `toDTO()` 私有方法完成 Entity → DTO 转换
- 事务管理在此层使用 `@Transactional`（如需要）

### 定时任务调度器
- `PointDistributionScheduler` 放在 `application.impl.scheduler` 包下
- 使用 `@Component` + `@RequiredArgsConstructor`
- `@Scheduled(cron = "0 0 2 1 * ?")` 触发每月发放
- 调用 PointDomainService 的方法完成发放逻辑
- 日志记录发放结果（总人数、成功数、失败数）

## 禁止事项
- application-impl 不允许直接依赖 repository-api 或任何基础设施接口
- 不允许在 DTO/Request 中包含业务逻辑
- 不允许在应用层抛出领域异常以外的自定义异常
