package com.awsome.shop.point.application.api.dto.point.request;

import lombok.Data;

@Data
public class UpdateRuleRequest {
    private String name;
    private String description;
    private String type;
    private String points;
    private String triggerCondition;
}
