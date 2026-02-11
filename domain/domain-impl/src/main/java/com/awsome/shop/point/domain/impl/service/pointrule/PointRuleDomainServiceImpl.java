package com.awsome.shop.point.domain.impl.service.pointrule;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.pointrule.PointRuleEntity;
import com.awsome.shop.point.domain.service.pointrule.PointRuleDomainService;
import com.awsome.shop.point.repository.pointrule.PointRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 积分规则领域服务实现
 */
@Service
@RequiredArgsConstructor
public class PointRuleDomainServiceImpl implements PointRuleDomainService {

    private final PointRuleRepository pointRuleRepository;

    @Override
    public PageResult<PointRuleEntity> page(int page, int size, String name, String ruleType, Integer status) {
        return pointRuleRepository.page(page, size, name, ruleType, status);
    }
}
