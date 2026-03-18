---
inclusion: fileMatch
fileMatchPattern: "interface/**"
---

# Interface 层编码规则

接口层是系统的入口，负责接收外部请求并委托给应用层处理。

## 子模块

### interface-http（HTTP 控制器）
- 使用 `@RestController` + `@RequestMapping` + `@RequiredArgsConstructor`
- 命名：`{Name}Controller`
- 包路径：`facade.http.controller`

#### 控制器分组
- `PointController`（`@RequestMapping("/api/points")`）— 员工端点
  - GET /balance — 查询当前用户积分余额（@RequestHeader("X-User-Id") Long userId）
  - GET /transactions — 查询当前用户积分变动历史
- `PointAdminController`（`@RequestMapping("/api/admin/points")`）— 管理员端点
  - GET /balances — 查看所有员工积分余额
  - GET /transactions/{userId} — 查看指定员工积分变动明细
  - POST /adjust — 手动调整员工积分（@RequestHeader("X-User-Id") Long operatorId）
  - GET /config — 获取发放配置
  - PUT /config — 更新发放配置
- `PointInternalController`（`@RequestMapping("/api/internal/points")`）— 内部端点
  - POST /init — 初始化用户积分余额
  - POST /deduct — 兑换扣除积分
  - POST /rollback — 回滚积分扣除（返回 Result<Void>）
  - GET /balance/{userId} — 查询指定用户积分余额

#### URL 设计规范
- URL 路径以设计文档（aidlc-docs/construction/points-service）定义的 API 契约为准
- 使用标准 RESTful HTTP 方法（GET 查询、POST 创建/操作、PUT 更新）
- 员工端点从 `X-User-Id` 请求头获取当前用户 ID（API 网关注入）
- 管理员端点从 `X-User-Id` 请求头获取操作人 ID（用于审计）
- 内部端点不需要 X-User-Id 请求头（服务间信任）
- GET 请求的查询参数直接用方法参数或 Request 对象接收（不用 `@RequestBody`）
- POST/PUT 请求体使用 `@RequestBody @Valid` 接收并校验
- 返回值统一使用 `Result<T>` 包装（`facade.http.response.Result`）
- Swagger 注解：类上 `@Tag(name = "...", description = "...")`，方法上 `@Operation(summary = "...")`

#### 统一响应格式
```json
{ "code": 0, "message": "操作成功", "data": {} }
```
- `code = 0` 表示成功，非 0 为错误码
- 使用 `Result.success()` / `Result.success(data)` / `Result.error(code, message)` 静态方法

#### 全局异常处理
- `GlobalExceptionHandler`（`@RestControllerAdvice`）统一捕获异常
- `BusinessException` → 根据错误码前缀自动映射 HTTP 状态码
- `ParameterException` → 400
- `MethodArgumentNotValidException` → 400（含字段级错误详情）
- `SystemException` / `Exception` → 500（不暴露内部细节）

### interface-consumer（消息消费者）
- 处理 SQS 消息，委托给应用层服务

## 禁止事项
- Controller 只调用 Application Service 接口，不直接调用 Domain Service 或 Repository
- interface 层不直接依赖 common，通过 application-api 传递获得
- 不在 Controller 中编写业务逻辑
- 不在 Controller 中进行数据转换（转换由 Application 层负责）
- 不直接返回领域实体，必须通过 DTO
