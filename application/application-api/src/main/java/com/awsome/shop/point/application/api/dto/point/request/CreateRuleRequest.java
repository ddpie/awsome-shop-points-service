package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRuleRequest {
    @NotBlank(message = "规则名称不能为空")
    private String name;
    private String description;
    @NotBlank(message = "规则类型不能为空")
    private String type;
    @NotBlank(message = "积分值不能为空")
    private String points;
    private String triggerCondition;
}
