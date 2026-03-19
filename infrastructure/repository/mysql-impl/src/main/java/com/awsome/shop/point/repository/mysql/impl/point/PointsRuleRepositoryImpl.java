package com.awsome.shop.point.repository.mysql.impl.point;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.point.PointsRuleEntity;
import com.awsome.shop.point.domain.model.point.RuleStatus;
import com.awsome.shop.point.domain.model.point.RuleType;
import com.awsome.shop.point.repository.mysql.mapper.point.PointsRuleMapper;
import com.awsome.shop.point.repository.mysql.po.point.PointsRulePO;
import com.awsome.shop.point.repository.point.PointsRuleRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PointsRuleRepositoryImpl implements PointsRuleRepository {

    private final PointsRuleMapper mapper;

    @Override
    public PageResult<PointsRuleEntity> findAll(int page, int size) {
        Page<PointsRulePO> p = mapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<PointsRulePO>().orderByDesc(PointsRulePO::getCreatedAt)
        );
        PageResult<PointsRuleEntity> result = new PageResult<>();
        result.setCurrentPage(p.getCurrent());
        result.setTotalElements(p.getTotal());
        result.setTotalPages(p.getPages());
        result.setContent(p.getRecords().stream().map(this::toEntity).collect(Collectors.toList()));
        return result;
    }

    @Override
    public Optional<PointsRuleEntity> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toEntity);
    }

    @Override
    public PointsRuleEntity save(PointsRuleEntity entity) {
        PointsRulePO po = toPO(entity);
        mapper.insert(po);
        return toEntity(po);
    }

    @Override
    public PointsRuleEntity update(PointsRuleEntity entity) {
        PointsRulePO po = toPO(entity);
        mapper.updateById(po);
        return findById(entity.getId()).orElse(entity);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    private PointsRuleEntity toEntity(PointsRulePO po) {
        PointsRuleEntity e = new PointsRuleEntity();
        e.setId(po.getId());
        e.setName(po.getName());
        e.setDescription(po.getDescription());
        e.setType(RuleType.valueOf(po.getType()));
        e.setPoints(po.getPoints());
        e.setTriggerCondition(po.getTriggerCondition());
        e.setStatus(RuleStatus.valueOf(po.getStatus()));
        e.setCreatedAt(po.getCreatedAt());
        e.setUpdatedAt(po.getUpdatedAt());
        return e;
    }

    private PointsRulePO toPO(PointsRuleEntity e) {
        PointsRulePO po = new PointsRulePO();
        po.setId(e.getId());
        po.setName(e.getName());
        po.setDescription(e.getDescription());
        po.setType(e.getType() != null ? e.getType().name() : null);
        po.setPoints(e.getPoints());
        po.setTriggerCondition(e.getTriggerCondition());
        po.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        return po;
    }
}
