package com.awsome.shop.point.facade.http.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 错误详情
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;
    private String message;

    public static ErrorDetail of(String field, String message) {
        return new ErrorDetail(field, message);
    }
}
