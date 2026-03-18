package com.awsome.shop.point.domain.service.point;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.point.PointBalanceEntity;
import com.awsome.shop.point.domain.model.point.PointTransactionEntity;
import com.awsome.shop.point.domain.model.point.TransactionType;

import java.util.List;

/**
 * 积分领域服务接口
 */
public interface PointDomainService {

    /** 初始化用户积分余额（幂等） */
    PointBalanceEntity initBalance(Long userId);

    /** 查询用户积分余额 */
    PointBalanceEntity getBalance(Long userId);

    /** 查询用户积分变动历史 */
    PageResult<PointTransactionEntity> getTransactions(Long userId, int page, int size, TransactionType type);

    /** 管理员查看所有员工积分余额 */
    PageResult<PointBalanceEntity> pageBalances(int page, int size, Long userId);

    /** 管理员手动调整积分 */
    PointTransactionEntity adjustPoints(Long userId, Integer amount, String remark, Long operatorId);

    /** 兑换扣除积分 */
    PointTransactionEntity deductPoints(Long userId, Integer amount, Long orderId);

    /** 兑换回滚积分 */
    void rollbackDeduction(Long transactionId);

    /** 获取发放配置（每月发放额度） */
    Integer getDistributionAmount();

    /** 更新发放配置 */
    void updateDistributionAmount(Integer amount);

    /** 获取所有有余额记录的用户（用于定时发放） */
    List<PointBalanceEntity> findAllBalances();

    /** 为单个用户发放积分（独立事务） */
    void distributeToUser(Long userId, Integer amount, String remark);
}
