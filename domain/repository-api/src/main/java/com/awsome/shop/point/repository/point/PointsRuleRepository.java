package com.awsome.shop.point.repository.point;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.point.PointsRuleEntity;

import java.util.Optional;

public interface PointsRuleRepository {

    PageResult<PointsRuleEntity> findAll(int page, int size);

    Optional<PointsRuleEntity> findById(Long id);

    PointsRuleEntity save(PointsRuleEntity entity);

    PointsRuleEntity update(PointsRuleEntity entity);

    void deleteById(Long id);
}
