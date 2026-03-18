package com.awsome.shop.point.application.api.dto.point;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分变动流水 DTO
 */
@Data
public class PointTransactionDTO {

    private Long id;
    private Long userId;
    private String type;
    private Integer amount;
    private Integer balanceAfter;
    private Long referenceId;
    private Long operatorId;
    private String remark;
    private LocalDateTime createdAt;
}
