package com.awsome.shop.point.repository.mysql.impl.point;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.point.PointBalanceEntity;
import com.awsome.shop.point.repository.mysql.mapper.point.PointBalanceMapper;
import com.awsome.shop.point.repository.mysql.po.point.PointBalancePO;
import com.awsome.shop.point.repository.point.PointBalanceRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分余额仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PointBalanceRepositoryImpl implements PointBalanceRepository {

    private final PointBalanceMapper pointBalanceMapper;

    @Override
    public PointBalanceEntity getByUserId(Long userId) {
        LambdaQueryWrapper<PointBalancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointBalancePO::getUserId, userId);
        PointBalancePO po = pointBalanceMapper.selectOne(wrapper);
        return po == null ? null : toEntity(po);
    }

    @Override
    public PointBalanceEntity getByUserIdForUpdate(Long userId) {
        PointBalancePO po = pointBalanceMapper.selectByUserIdForUpdate(userId);
        return po == null ? null : toEntity(po);
    }

    @Override
    public void save(PointBalanceEntity entity) {
        PointBalancePO po = toPO(entity);
        pointBalanceMapper.insert(po);
        entity.setId(po.getId());
    }

    @Override
    public void updateBalance(Long userId, Integer newBalance) {
        pointBalanceMapper.updateBalanceByUserId(userId, newBalance);
    }

    @Override
    public Integer addBalanceAtomic(Long userId, Integer amount) {
        pointBalanceMapper.addBalanceByUserId(userId, amount);
        return pointBalanceMapper.selectBalanceByUserId(userId);
    }

    @Override
    public PageResult<PointBalanceEntity> page(int page, int size, Long userId) {
        LambdaQueryWrapper<PointBalancePO> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(PointBalancePO::getUserId, userId);
        }
        wrapper.orderByDesc(PointBalancePO::getUpdatedAt);

        IPage<PointBalancePO> result = pointBalanceMapper.selectPage(new Page<>(page, size), wrapper);

        PageResult<PointBalanceEntity> pageResult = new PageResult<>();
        pageResult.setCurrentPage(result.getCurrent());
        pageResult.setTotalElements(result.getTotal());
        pageResult.setTotalPages(result.getPages());
        pageResult.setContent(result.getRecords().stream().map(this::toEntity).collect(Collectors.toList()));
        return pageResult;
    }

    @Override
    public List<PointBalanceEntity> findAll() {
        List<PointBalancePO> list = pointBalanceMapper.selectList(new LambdaQueryWrapper<>());
        return list.stream().map(this::toEntity).collect(Collectors.toList());
    }

    private PointBalanceEntity toEntity(PointBalancePO po) {
        PointBalanceEntity entity = new PointBalanceEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setBalance(po.getBalance());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private PointBalancePO toPO(PointBalanceEntity entity) {
        PointBalancePO po = new PointBalancePO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setBalance(entity.getBalance());
        return po;
    }
}
