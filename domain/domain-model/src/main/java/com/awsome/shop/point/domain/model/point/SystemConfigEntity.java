package com.awsome.shop.point.domain.model.point;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置领域实体
 */
@Data
public class SystemConfigEntity {

    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updatedAt;
}
