package com.awsome.shop.point.domain.model.point;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分余额领域实体
 */
@Data
public class PointBalanceEntity {

    private Long id;
    private Long userId;
    private Integer balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 增加余额
     */
    public void addBalance(int amount) {
        this.balance += amount;
    }

    /**
     * 扣除余额（调用前需校验余额充足）
     */
    public void deductBalance(int amount) {
        this.balance -= amount;
    }

    /**
     * 校验余额是否足够扣除
     */
    public boolean hasSufficientBalance(int amount) {
        return this.balance >= amount;
    }
}
