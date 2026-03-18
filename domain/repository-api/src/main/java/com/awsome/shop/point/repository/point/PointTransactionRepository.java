package com.awsome.shop.point.repository.point;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.point.PointTransactionEntity;
import com.awsome.shop.point.domain.model.point.TransactionType;

/**
 * 积分变动流水仓储接口
 */
public interface PointTransactionRepository {

    void save(PointTransactionEntity entity);

    PointTransactionEntity getById(Long id);

    PageResult<PointTransactionEntity> pageByUserId(Long userId, int page, int size, TransactionType type);

    /**
     * 检查是否存在指定 referenceId 的回滚记录
     */
    boolean existsRollbackByReferenceId(Long referenceId);
}
