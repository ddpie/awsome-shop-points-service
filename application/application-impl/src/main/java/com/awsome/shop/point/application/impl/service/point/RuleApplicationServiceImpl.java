package com.awsome.shop.point.application.impl.service.point;

import com.awsome.shop.point.application.api.dto.point.PointsRuleDTO;
import com.awsome.shop.point.application.api.dto.point.request.CreateRuleRequest;
import com.awsome.shop.point.application.api.dto.point.request.UpdateRuleRequest;
import com.awsome.shop.point.application.api.service.point.RuleApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.point.PointsRuleEntity;
import com.awsome.shop.point.domain.model.point.RuleStatus;
import com.awsome.shop.point.domain.model.point.RuleType;
import com.awsome.shop.point.domain.service.point.PointDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RuleApplicationServiceImpl implements RuleApplicationService {

    private final PointDomainService pointDomainService;

    @Override
    public PageResult<PointsRuleDTO> listRules(int page, int size) {
        return pointDomainService.listRules(page, size).convert(this::toDTO);
    }

    @Override
    public PointsRuleDTO createRule(CreateRuleRequest request) {
        PointsRuleEntity entity = new PointsRuleEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setType(RuleType.valueOf(request.getType()));
        entity.setPoints(request.getPoints());
        entity.setTriggerCondition(request.getTriggerCondition());
        entity.setStatus(RuleStatus.ACTIVE);
        return toDTO(pointDomainService.createRule(entity));
    }

    @Override
    public PointsRuleDTO updateRule(Long id, UpdateRuleRequest request) {
        PointsRuleEntity entity = pointDomainService.getRuleById(id);
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getType() != null) entity.setType(RuleType.valueOf(request.getType()));
        if (request.getPoints() != null) entity.setPoints(request.getPoints());
        if (request.getTriggerCondition() != null) entity.setTriggerCondition(request.getTriggerCondition());
        return toDTO(pointDomainService.updateRule(entity));
    }

    @Override
    public PointsRuleDTO updateRuleStatus(Long id, String status) {
        PointsRuleEntity entity = pointDomainService.getRuleById(id);
        entity.setStatus(RuleStatus.valueOf(status));
        return toDTO(pointDomainService.updateRule(entity));
    }

    @Override
    public void deleteRule(Long id) {
        pointDomainService.deleteRule(id);
    }

    private PointsRuleDTO toDTO(PointsRuleEntity entity) {
        PointsRuleDTO dto = new PointsRuleDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType().name());
        dto.setPoints(entity.getPoints());
        dto.setTriggerCondition(entity.getTriggerCondition());
        dto.setStatus(entity.getStatus().name());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
