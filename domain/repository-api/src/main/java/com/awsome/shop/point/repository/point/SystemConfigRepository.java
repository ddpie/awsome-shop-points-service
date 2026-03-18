package com.awsome.shop.point.repository.point;

import com.awsome.shop.point.domain.model.point.SystemConfigEntity;

/**
 * 系统配置仓储接口
 */
public interface SystemConfigRepository {

    SystemConfigEntity getByKey(String configKey);

    void upsert(SystemConfigEntity entity);
}
