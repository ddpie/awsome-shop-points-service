package com.awsome.shop.point.application.api.service.point;

import com.awsome.shop.point.application.api.dto.point.PointsRuleDTO;
import com.awsome.shop.point.application.api.dto.point.request.CreateRuleRequest;
import com.awsome.shop.point.application.api.dto.point.request.UpdateRuleRequest;
import com.awsome.shop.point.common.dto.PageResult;

public interface RuleApplicationService {

    PageResult<PointsRuleDTO> listRules(int page, int size);

    PointsRuleDTO createRule(CreateRuleRequest request);

    PointsRuleDTO updateRule(Long id, UpdateRuleRequest request);

    PointsRuleDTO updateRuleStatus(Long id, String status);

    void deleteRule(Long id);
}
