package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新发放配置请求
 */
@Data
public class UpdateDistributionConfigRequest {

    @NotNull(message = "发放额度不能为空")
    @Min(value = 1, message = "发放额度必须大于0")
    private Integer amount;
}
