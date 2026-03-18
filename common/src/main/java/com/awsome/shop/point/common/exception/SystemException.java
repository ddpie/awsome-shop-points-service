package com.awsome.shop.point.common.exception;

import com.awsome.shop.point.common.enums.ErrorCode;

/**
 * 系统异常
 */
public class SystemException extends BaseException {

    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SystemException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
