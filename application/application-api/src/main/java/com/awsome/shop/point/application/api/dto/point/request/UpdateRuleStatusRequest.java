package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRuleStatusRequest {
    @NotBlank(message = "状态不能为空")
    private String status;
}
