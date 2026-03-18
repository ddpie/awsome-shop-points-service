package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询用户积分余额请求（内部接口）
 */
@Data
public class GetBalanceRequest {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;
}
