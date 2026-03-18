package com.awsome.shop.point.domain.model.point;

/**
 * 积分变动类型枚举
 */
public enum TransactionType {

    /** 系统自动发放 */
    DISTRIBUTION,
    /** 管理员手动增加 */
    MANUAL_ADD,
    /** 管理员手动扣除 */
    MANUAL_DEDUCT,
    /** 兑换扣除 */
    REDEMPTION,
    /** 兑换回滚 */
    ROLLBACK
}
