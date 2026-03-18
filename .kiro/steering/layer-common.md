---
inclusion: fileMatch
fileMatchPattern: "common/**"
---

# Common 层编码规则

本模块是全局共享基础设施，所有其他模块都可以依赖它。不要在此引入 Spring 框架依赖。

## 职责范围
- 异常体系定义
- 错误码接口与枚举
- 通用 DTO（如 `PageResult`）
- 通用注解
- 统一响应包装（`Result`）

## 异常体系
- 基类 `BaseException` 包含 `errorCode`（String）和 `errorMessage`（String）
- 三个子类：`BusinessException`（业务异常）、`ParameterException`（参数异常）、`SystemException`（系统异常）
- 支持 `ErrorCode` 枚举 + `MessageFormat` 占位符（`{0}`, `{1}`）参数化消息

## 错误码规范
- 实现 `ErrorCode` 接口，提供 `getCode()` 和 `getMessage()`
- 按业务领域分组为独立枚举类（如 `PointsErrorCode`）
- 格式：`{CATEGORY}_{SEQ}`，类别前缀决定 HTTP 状态码映射：
  - `AUTH_` → 401, `AUTHZ_` → 403, `PARAM_` → 400
  - `NOT_FOUND_` → 404, `CONFLICT_` → 409, `LOCKED_` → 423
  - `BIZ_` → 200, `SYS_` → 500
- 注意：设计文档中使用 `POINTS_` 前缀（如 `POINTS_001`），实际实现使用 `{CATEGORY}_{SEQ}` 格式以保持与 GlobalExceptionHandler 的 HTTP 状态码自动映射一致

### 积分服务错误码（PointsErrorCode）
| 枚举值 | 错误码 | 消息 | 对应设计文档 |
|--------|--------|------|-------------|
| BALANCE_NOT_FOUND | NOT_FOUND_001 | 积分余额记录不存在 | POINTS_001 |
| INSUFFICIENT_BALANCE_ADJUST | BAD_REQUEST_001 | 扣除后余额不足 | POINTS_002 |
| INSUFFICIENT_BALANCE_REDEEM | BAD_REQUEST_002 | 积分不足，无法兑换 | POINTS_003 |
| TRANSACTION_NOT_FOUND | NOT_FOUND_002 | 积分变动记录不存在 | POINTS_004 |
| INVALID_ROLLBACK_TYPE | BAD_REQUEST_003 | 只能回滚兑换扣除记录 | POINTS_005 |
| DUPLICATE_ROLLBACK | CONFLICT_001 | 该笔扣除已回滚，不可重复操作 | POINTS_006 |

## 编码约定
- 使用 Lombok `@Data`、`@Getter` 简化代码
- 类注释使用中文 Javadoc
- 不依赖 Spring 框架
- 不依赖任何业务模块
