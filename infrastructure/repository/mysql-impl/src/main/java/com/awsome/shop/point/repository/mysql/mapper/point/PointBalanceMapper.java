package com.awsome.shop.point.repository.mysql.mapper.point;

import com.awsome.shop.point.repository.mysql.po.point.PointBalancePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 积分余额 Mapper
 */
@Mapper
public interface PointBalanceMapper extends BaseMapper<PointBalancePO> {

    /**
     * 悲观锁查询
     */
    @Select("SELECT * FROM point_balances WHERE user_id = #{userId} AND deleted = 0 FOR UPDATE")
    PointBalancePO selectByUserIdForUpdate(@Param("userId") Long userId);

    /**
     * 直接更新余额（绕过乐观锁，配合悲观锁使用）
     */
    @Update("UPDATE point_balances SET balance = #{balance}, updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int updateBalanceByUserId(@Param("userId") Long userId, @Param("balance") Integer balance);
}
