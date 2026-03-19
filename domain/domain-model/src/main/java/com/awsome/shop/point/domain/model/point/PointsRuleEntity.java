package com.awsome.shop.point.domain.model.point;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointsRuleEntity {
    private Long id;
    private String name;
    private String description;
    private RuleType type;
    private String points;
    private String triggerCondition;
    private RuleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
