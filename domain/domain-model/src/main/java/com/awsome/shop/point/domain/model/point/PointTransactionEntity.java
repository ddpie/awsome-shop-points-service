package com.awsome.shop.point.domain.model.point;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分变动流水领域实体
 */
@Data
public class PointTransactionEntity {

    private Long id;
    private Long userId;
    private TransactionType type;
    private Integer amount;
    private Integer balanceAfter;
    private Long referenceId;
    private Long operatorId;
    private String remark;
    private LocalDateTime createdAt;
}
