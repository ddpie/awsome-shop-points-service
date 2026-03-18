package com.awsome.shop.point.repository.mysql.mapper.point;

import com.awsome.shop.point.repository.mysql.po.point.PointTransactionPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分变动流水 Mapper
 */
@Mapper
public interface PointTransactionMapper extends BaseMapper<PointTransactionPO> {
}
