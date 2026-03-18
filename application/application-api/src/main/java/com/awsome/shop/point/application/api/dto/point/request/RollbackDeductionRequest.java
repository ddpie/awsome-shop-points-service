package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 兑换回滚请求（内部接口）
 */
@Data
public class RollbackDeductionRequest {

    @NotNull(message = "变动记录ID不能为空")
    @Min(value = 1, message = "变动记录ID必须大于0")
    private Long transactionId;
}
