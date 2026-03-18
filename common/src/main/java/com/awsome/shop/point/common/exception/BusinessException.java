package com.awsome.shop.point.common.exception;

import com.awsome.shop.point.common.enums.ErrorCode;

/**
 * 业务异常
 */
public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
