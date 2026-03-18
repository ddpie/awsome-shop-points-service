package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 查询积分变动历史请求
 */
@Data
public class QueryTransactionsRequest {

    @Min(value = 0, message = "页码最小为0")
    private Integer page = 0;

    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 20;

    /** 变动类型筛选（可选） */
    private String type;
}
