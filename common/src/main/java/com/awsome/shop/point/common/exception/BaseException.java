package com.awsome.shop.point.common.exception;

import com.awsome.shop.point.common.enums.ErrorCode;
import lombok.Getter;

import java.text.MessageFormat;

/**
 * 基础异常类
 */
@Getter
public class BaseException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.errorMessage = errorCode.getMessage();
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode.getCode();
        this.errorMessage = errorCode.getMessage();
    }

    public BaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode.getCode();
        this.errorMessage = customMessage;
    }

    public BaseException(ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode.getMessage(), args));
        this.errorCode = errorCode.getCode();
        this.errorMessage = formatMessage(errorCode.getMessage(), args);
    }

    public BaseException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BaseException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private static String formatMessage(String template, Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }
        try {
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }
}
