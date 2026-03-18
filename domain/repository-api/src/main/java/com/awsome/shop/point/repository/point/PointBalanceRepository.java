package com.awsome.shop.point.repository.point;

import com.awsome.shop.point.domain.model.point.PointBalanceEntity;
import com.awsome.shop.point.common.dto.PageResult;

import java.util.List;

/**
 * 积分余额仓储接口
 */
public interface PointBalanceRepository {

    PointBalanceEntity getByUserId(Long userId);

    /**
     * 悲观锁查询（SELECT ... FOR UPDATE）
     */
    PointBalanceEntity getByUserIdForUpdate(Long userId);

    void save(PointBalanceEntity entity);

    void updateBalance(Long userId, Integer newBalance);

    /**
     * 原子增加余额（用于定时发放，不使用悲观锁）
     * @return 更新后的余额
     */
    Integer addBalanceAtomic(Long userId, Integer amount);

    PageResult<PointBalanceEntity> page(int page, int size, Long userId);

    List<PointBalanceEntity> findAll();
}
