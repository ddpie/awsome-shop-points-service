package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 积分初始化请求（内部接口）
 */
@Data
public class InitPointsRequest {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @Min(value = 0, message = "初始余额不能为负数")
    private Integer initialBalance;
}
