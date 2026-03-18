package com.awsome.shop.point.repository.mysql.impl.point;

import com.awsome.shop.point.domain.model.point.SystemConfigEntity;
import com.awsome.shop.point.repository.mysql.mapper.point.SystemConfigMapper;
import com.awsome.shop.point.repository.mysql.po.point.SystemConfigPO;
import com.awsome.shop.point.repository.point.SystemConfigRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 系统配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class SystemConfigRepositoryImpl implements SystemConfigRepository {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public SystemConfigEntity getByKey(String configKey) {
        LambdaQueryWrapper<SystemConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfigPO::getConfigKey, configKey);
        SystemConfigPO po = systemConfigMapper.selectOne(wrapper);
        return po == null ? null : toEntity(po);
    }

    @Override
    public void upsert(SystemConfigEntity entity) {
        LambdaQueryWrapper<SystemConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfigPO::getConfigKey, entity.getConfigKey());
        SystemConfigPO existing = systemConfigMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setConfigValue(entity.getConfigValue());
            existing.setDescription(entity.getDescription());
            systemConfigMapper.updateById(existing);
        } else {
            SystemConfigPO po = toPO(entity);
            systemConfigMapper.insert(po);
            entity.setId(po.getId());
        }
    }

    private SystemConfigEntity toEntity(SystemConfigPO po) {
        SystemConfigEntity entity = new SystemConfigEntity();
        entity.setId(po.getId());
        entity.setConfigKey(po.getConfigKey());
        entity.setConfigValue(po.getConfigValue());
        entity.setDescription(po.getDescription());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private SystemConfigPO toPO(SystemConfigEntity entity) {
        SystemConfigPO po = new SystemConfigPO();
        po.setConfigKey(entity.getConfigKey());
        po.setConfigValue(entity.getConfigValue());
        po.setDescription(entity.getDescription());
        return po;
    }
}
