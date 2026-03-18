package com.awsome.shop.point.common.enums;

/**
 * 积分业务错误码
 */
public enum PointsErrorCode implements ErrorCode {

    BALANCE_NOT_FOUND("NOT_FOUND_001", "积分余额记录不存在"),
    INSUFFICIENT_BALANCE_ADJUST("BIZ_001", "扣除后余额不足"),
    INSUFFICIENT_BALANCE_REDEEM("BIZ_002", "积分不足，无法兑换"),
    TRANSACTION_NOT_FOUND("NOT_FOUND_002", "积分变动记录不存在"),
    INVALID_ROLLBACK_TYPE("BIZ_003", "只能回滚兑换扣除记录"),
    DUPLICATE_ROLLBACK("CONFLICT_001", "该笔扣除已回滚，不可重复操作"),
    CONFIG_NOT_FOUND("NOT_FOUND_003", "配置项不存在");

    private final String code;
    private final String message;

    PointsErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
