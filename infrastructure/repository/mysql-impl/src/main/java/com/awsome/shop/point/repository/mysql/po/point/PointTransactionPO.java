package com.awsome.shop.point.repository.mysql.po.point;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分变动流水持久化对象
 */
@Data
@TableName("point_transactions")
public class PointTransactionPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private Integer amount;

    private Integer balanceAfter;

    private Long referenceId;

    private Long operatorId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
