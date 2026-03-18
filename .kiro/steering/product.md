# 积分服务（points-service）产品规范

Awsome Shop Point Service — 电商平台积分微服务，基于 DDD + 六边形架构构建。

## 服务定位
- 独立微服务，端口 8003，数据库 points_db
- 被 auth-service（注册初始化）、order-service（兑换扣除/回滚/查询余额）调用
- 不主动调用其他微服务

## 领域实体

### PointBalance（积分余额）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录ID |
| userId | Long | 用户ID（唯一） |
| balance | Integer | 当前积分余额（≥ 0） |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

### PointTransaction（积分变动流水）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 流水ID |
| userId | Long | 用户ID |
| type | TransactionType | 变动类型 |
| amount | Integer | 变动数量（正数增加，负数减少） |
| balanceAfter | Integer | 变动后余额 |
| referenceId | Long? | 关联ID（兑换订单ID等） |
| operatorId | Long? | 操作人ID（手动调整时） |
| remark | String? | 备注 |
| createdAt | DateTime | 创建时间 |

### TransactionType（变动类型枚举）
| 值 | 说明 | amount 符号 |
|----|------|-------------|
| DISTRIBUTION | 系统自动发放 | 正数 |
| MANUAL_ADD | 管理员手动增加 | 正数 |
| MANUAL_DEDUCT | 管理员手动扣除 | 负数 |
| REDEMPTION | 兑换扣除 | 负数 |
| ROLLBACK | 兑换回滚 | 正数 |

### SystemConfig（系统配置）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 配置ID |
| configKey | String | 配置键（唯一） |
| configValue | String | 配置值 |
| description | String? | 配置说明 |
| updatedAt | DateTime | 更新时间 |

预置配置项：`points.distribution.amount = 100`（每月自动发放积分额度）

### DistributionBatch（发放批次）— 设计文档要求
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 批次ID |
| distributionAmount | Integer | 本次发放额度 |
| totalCount | Integer | 应发放总人数 |
| successCount | Integer | 成功发放人数 |
| failCount | Integer | 失败人数 |
| status | Enum(RUNNING/COMPLETED/FAILED) | 批次状态 |
| startedAt | DateTime | 开始时间 |
| completedAt | DateTime? | 完成时间 |

## API 端点定义

### 员工端点（需认证，X-User-Id 请求头）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/points/balance | 查询当前用户积分余额 |
| GET | /api/points/transactions?page=&size= | 查询当前用户积分变动历史 |

### 管理员端点（需管理员角色）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/points/balances?page=&size=&keyword= | 查看所有员工积分余额 |
| GET | /api/admin/points/transactions/{userId}?page=&size=&type= | 查看指定员工积分变动明细 |
| POST | /api/admin/points/adjust | 手动调整员工积分 |
| GET | /api/admin/points/config | 获取发放配置 |
| PUT | /api/admin/points/config | 更新发放配置 |

### 内部端点（服务间调用，不经过网关，无需 JWT）
| 方法 | 路径 | 调用方 | 说明 |
|------|------|--------|------|
| POST | /api/internal/points/init | auth-service | 初始化用户积分余额 |
| POST | /api/internal/points/deduct | order-service | 兑换扣除积分 |
| POST | /api/internal/points/rollback | order-service | 回滚积分扣除 |
| GET | /api/internal/points/balance/{userId} | order-service | 查询指定用户积分余额 |

## 业务规则

### BR-001: 积分余额非负
任何扣除操作都必须校验扣除后余额 ≥ 0

### BR-002: 积分初始化幂等
userId 已存在余额记录时直接返回，不创建重复记录，初始余额固定为 0

### BR-003: 积分变动必须记录流水
任何积分变动都必须同时创建 point_transactions 记录，余额更新和流水创建在同一事务中

### BR-004: 悲观锁并发控制
所有涉及余额变动的操作必须使用 `SELECT ... FOR UPDATE` 锁定余额行

### BR-005: 回滚唯一性
同一笔兑换扣除只能回滚一次，通过查询是否存在 type=ROLLBACK 且 referenceId 相同的记录判断

### BR-006: 回滚类型限制
只能回滚 type=REDEMPTION 的变动记录

### BR-007: 手动调整必须填写备注
管理员手动调整积分时 remark 字段为必填

### BR-008: 自动发放独立事务
定时任务批量发放时，每位用户的发放操作为独立事务（REQUIRES_NEW），单条失败不影响其他用户

### BR-009: 发放配置默认值
system_configs 中不存在配置项时使用默认值 100

### BR-010: 变动类型与 amount 符号一致性
DISTRIBUTION/MANUAL_ADD/ROLLBACK: amount > 0；MANUAL_DEDUCT/REDEMPTION: amount < 0

## 错误码

| 错误码 | HTTP 状态码 | 消息 | 触发场景 |
|--------|-----------|------|---------|
| NOT_FOUND_001 | 404 | 积分余额记录不存在 | 查询/操作时 userId 无对应余额记录 |
| BIZ_001 | 200 | 扣除后余额不足 | 管理员手动扣除时余额不足 |
| BIZ_002 | 200 | 积分不足，无法兑换 | 兑换扣除时余额不足 |
| NOT_FOUND_002 | 404 | 积分变动记录不存在 | 回滚时 transactionId 无对应记录 |
| BIZ_003 | 200 | 只能回滚兑换扣除记录 | 回滚时原始记录 type ≠ REDEMPTION |
| CONFLICT_001 | 409 | 该笔扣除已回滚，不可重复操作 | 重复回滚 |
| NOT_FOUND_003 | 404 | 配置项不存在 | 内部错误（有默认值兜底） |

注意：设计文档使用 POINTS_xxx 前缀，实际实现使用 {CATEGORY}_{SEQ} 格式以配合 GlobalExceptionHandler 的 HTTP 状态码自动映射。

## 校验规则

### 积分初始化
- userId: 必填，> 0

### 手动调整积分
- userId: 必填，> 0
- amount: 必填，≠ 0
- remark: 必填，非空，最长 500 字符

### 兑换扣除积分
- userId: 必填，> 0
- amount: 必填，> 0
- orderId: 必填，> 0

### 兑换回滚
- transactionId: 必填，> 0

### 更新发放配置
- amount: 必填，> 0

### 分页参数
- page: 默认 1，≥ 1（MyBatis-Plus 分页从 1 开始）
- size: 默认 20，范围 1 ~ 100

## 定时任务
- cron: `0 0 2 1 * ?`（每月1日凌晨2:00）
- 仅为 point_balances 表中已有记录的用户发放
- 发放备注格式：`系统自动发放 - YYYY年MM月`
- 日志记录：发放总人数、成功数、失败数

## 跨服务交互
- auth-service → POST /api/internal/points/init（注册成功后，失败不影响注册）
- order-service → POST /api/internal/points/deduct / rollback，GET /api/internal/points/balance/{userId}
- 内部接口仅在 Docker 内部网络可访问，不需要 JWT 认证
