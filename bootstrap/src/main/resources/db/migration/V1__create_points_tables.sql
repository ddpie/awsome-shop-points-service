-- V1: 创建积分服务核心表

-- 积分余额表
CREATE TABLE `point_balances` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `balance`     INT          NOT NULL DEFAULT 0 COMMENT '当前积分余额',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`  BIGINT                DEFAULT NULL COMMENT '创建人',
    `updated_by`  BIGINT                DEFAULT NULL COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分余额表';

-- 积分变动流水表
CREATE TABLE `point_transactions` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流水ID',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `type`          VARCHAR(20)  NOT NULL COMMENT '变动类型: DISTRIBUTION/MANUAL_ADD/MANUAL_DEDUCT/REDEMPTION/ROLLBACK',
    `amount`        INT          NOT NULL COMMENT '变动数量（正数增加，负数减少）',
    `balance_after` INT          NOT NULL COMMENT '变动后余额',
    `reference_id`  BIGINT                DEFAULT NULL COMMENT '关联ID（兑换订单ID等）',
    `operator_id`   BIGINT                DEFAULT NULL COMMENT '操作人ID（手动调整时）',
    `remark`        VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`    BIGINT                DEFAULT NULL COMMENT '创建人',
    `updated_by`    BIGINT                DEFAULT NULL COMMENT '更新人',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version`       INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_reference_id` (`reference_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分变动流水表';

-- 系统配置表
CREATE TABLE `system_configs` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
    `description`  VARCHAR(500)          DEFAULT NULL COMMENT '配置说明',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`   BIGINT                DEFAULT NULL COMMENT '创建人',
    `updated_by`   BIGINT                DEFAULT NULL COMMENT '更新人',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version`      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_config_key` (`config_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表';

-- 插入默认发放配置
INSERT INTO `system_configs` (`config_key`, `config_value`, `description`)
VALUES ('points.distribution.amount', '100', '每月自动发放积分额度');
