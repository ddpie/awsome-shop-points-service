package com.awsome.shop.point.repository.mysql.impl.pointrule;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.pointrule.PointRuleEntity;
import com.awsome.shop.point.repository.mysql.mapper.pointrule.PointRuleMapper;
import com.awsome.shop.point.repository.mysql.po.pointrule.PointRulePO;
import com.awsome.shop.point.repository.pointrule.PointRuleRepository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

/**
 * 积分规则仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PointRuleRepositoryImpl implements PointRuleRepository {

    private final PointRuleMapper pointRuleMapper;

    @Override
    public PageResult<PointRuleEntity> page(int page, int size, String name, String ruleType, Integer status) {
        IPage<PointRulePO> result = pointRuleMapper.selectPage(new Page<>(page, size), name, ruleType, status);

        PageResult<PointRuleEntity> pageResult = new PageResult<>();
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(result.getRecords().stream().map(this::toEntity).collect(Collectors.toList()));
        return pageResult;
    }

    private PointRuleEntity toEntity(PointRulePO po) {
        PointRuleEntity entity = new PointRuleEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setDescription(po.getDescription());
        entity.setRuleType(po.getRuleType());
        entity.setPointValueMin(po.getPointValueMin());
        entity.setPointValueMax(po.getPointValueMax());
        entity.setTriggerCondition(po.getTriggerCondition());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
