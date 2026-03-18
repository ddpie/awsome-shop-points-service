package com.awsome.shop.point.application.api.dto.point;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发放配置 DTO
 */
@Data
public class DistributionConfigDTO {

    private Integer amount;
    private LocalDateTime updatedAt;
}
