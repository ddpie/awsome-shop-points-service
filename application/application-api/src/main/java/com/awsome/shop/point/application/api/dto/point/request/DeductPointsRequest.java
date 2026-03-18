package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 兑换扣除积分请求（内部接口）
 */
@Data
public class DeductPointsRequest {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @NotNull(message = "扣除数量不能为空")
    @Min(value = 1, message = "扣除数量必须大于0")
    private Integer amount;

    @NotNull(message = "订单ID不能为空")
    @Min(value = 1, message = "订单ID必须大于0")
    private Long orderId;
}
