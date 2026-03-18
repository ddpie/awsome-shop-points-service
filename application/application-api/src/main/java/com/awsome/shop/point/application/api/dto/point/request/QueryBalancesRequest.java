package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 查询所有员工积分余额请求
 */
@Data
public class QueryBalancesRequest {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 20;

    /** 按 userId 精确匹配 */
    private Long keyword;
}
