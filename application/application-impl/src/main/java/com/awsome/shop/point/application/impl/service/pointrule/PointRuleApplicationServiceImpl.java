package com.awsome.shop.point.application.impl.service.pointrule;

import com.awsome.shop.point.application.api.dto.pointrule.PointRuleDTO;
import com.awsome.shop.point.application.api.dto.pointrule.request.ListPointRuleRequest;
import com.awsome.shop.point.application.api.service.pointrule.PointRuleApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.pointrule.PointRuleEntity;
import com.awsome.shop.point.domain.service.pointrule.PointRuleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 积分规则应用服务实现
 *
 * <p>只依赖 Domain Service，不直接依赖 Repository</p>
 */
@Service
@RequiredArgsConstructor
public class PointRuleApplicationServiceImpl implements PointRuleApplicationService {

    private final PointRuleDomainService pointRuleDomainService;

    @Override
    public PageResult<PointRuleDTO> list(ListPointRuleRequest request) {
        PageResult<PointRuleEntity> page = pointRuleDomainService.page(
                request.getPage(), request.getSize(),
                request.getName(), request.getRuleType(), request.getStatus());
        return page.convert(this::toDTO);
    }

    private PointRuleDTO toDTO(PointRuleEntity entity) {
        PointRuleDTO dto = new PointRuleDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setRuleType(entity.getRuleType());
        dto.setPointValueMin(entity.getPointValueMin());
        dto.setPointValueMax(entity.getPointValueMax());
        dto.setTriggerCondition(entity.getTriggerCondition());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
