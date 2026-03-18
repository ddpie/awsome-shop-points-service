package com.awsome.shop.point.repository.mysql.impl.point;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.point.PointTransactionEntity;
import com.awsome.shop.point.domain.model.point.TransactionType;
import com.awsome.shop.point.repository.mysql.mapper.point.PointTransactionMapper;
import com.awsome.shop.point.repository.mysql.po.point.PointTransactionPO;
import com.awsome.shop.point.repository.point.PointTransactionRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

/**
 * 积分变动流水仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PointTransactionRepositoryImpl implements PointTransactionRepository {

    private final PointTransactionMapper pointTransactionMapper;

    @Override
    public void save(PointTransactionEntity entity) {
        PointTransactionPO po = toPO(entity);
        pointTransactionMapper.insert(po);
        entity.setId(po.getId());
    }

    @Override
    public PointTransactionEntity getById(Long id) {
        PointTransactionPO po = pointTransactionMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public PageResult<PointTransactionEntity> pageByUserId(Long userId, int page, int size, TransactionType type) {
        LambdaQueryWrapper<PointTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointTransactionPO::getUserId, userId);
        if (type != null) {
            wrapper.eq(PointTransactionPO::getType, type.name());
        }
        wrapper.orderByDesc(PointTransactionPO::getCreatedAt);

        IPage<PointTransactionPO> result = pointTransactionMapper.selectPage(new Page<>(page, size), wrapper);

        PageResult<PointTransactionEntity> pageResult = new PageResult<>();
        pageResult.setCurrentPage(result.getCurrent());
        pageResult.setTotalElements(result.getTotal());
        pageResult.setTotalPages(result.getPages());
        pageResult.setContent(result.getRecords().stream().map(this::toEntity).collect(Collectors.toList()));
        return pageResult;
    }

    @Override
    public boolean existsRollbackByReferenceId(Long referenceId) {
        LambdaQueryWrapper<PointTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointTransactionPO::getType, TransactionType.ROLLBACK.name());
        wrapper.eq(PointTransactionPO::getReferenceId, referenceId);
        return pointTransactionMapper.selectCount(wrapper) > 0;
    }

    private PointTransactionEntity toEntity(PointTransactionPO po) {
        PointTransactionEntity entity = new PointTransactionEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setType(TransactionType.valueOf(po.getType()));
        entity.setAmount(po.getAmount());
        entity.setBalanceAfter(po.getBalanceAfter());
        entity.setReferenceId(po.getReferenceId());
        entity.setOperatorId(po.getOperatorId());
        entity.setRemark(po.getRemark());
        entity.setCreatedAt(po.getCreatedAt());
        return entity;
    }

    private PointTransactionPO toPO(PointTransactionEntity entity) {
        PointTransactionPO po = new PointTransactionPO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setType(entity.getType().name());
        po.setAmount(entity.getAmount());
        po.setBalanceAfter(entity.getBalanceAfter());
        po.setReferenceId(entity.getReferenceId());
        po.setOperatorId(entity.getOperatorId());
        po.setRemark(entity.getRemark());
        return po;
    }
}
