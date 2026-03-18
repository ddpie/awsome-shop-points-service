package com.awsome.shop.point.facade.http.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应对象
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;

    private Result() {}

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(0, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "操作成功", data);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
