package com.awsome.shop.point.common.enums;

/**
 * 系统错误码
 */
public enum SystemErrorCode implements ErrorCode {

    DATABASE_ERROR("SYS_001", "系统异常，请稍后重试"),
    UNKNOWN_ERROR("SYS_002", "系统错误，请稍后重试");

    private final String code;
    private final String message;

    SystemErrorCode(String code, String message) {
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
