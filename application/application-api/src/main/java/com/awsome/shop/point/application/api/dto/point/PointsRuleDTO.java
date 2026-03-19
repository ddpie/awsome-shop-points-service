package com.awsome.shop.point.application.api.dto.point;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointsRuleDTO {
    private Long id;
    private String name;
    private String description;
    private String type;
    private String points;
    private String triggerCondition;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
