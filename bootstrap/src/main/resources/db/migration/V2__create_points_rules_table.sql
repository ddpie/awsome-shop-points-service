-- 积分规则表
CREATE TABLE points_rules (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL COMMENT '规则名称',
    description VARCHAR(500)           COMMENT '规则描述',
    type        VARCHAR(20)   NOT NULL COMMENT '规则类型: FIXED/PERFORMANCE/HOLIDAY/PROJECT/ONBOARDING/OTHER',
    points      VARCHAR(50)   NOT NULL COMMENT '积分值或范围',
    trigger_condition VARCHAR(200)     COMMENT '触发条件描述',
    status      VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  BIGINT                 DEFAULT NULL,
    updated_by  BIGINT                 DEFAULT NULL,
    deleted     TINYINT       NOT NULL DEFAULT 0,
    version     INT           NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';

-- 初始数据
INSERT INTO points_rules (name, description, type, points, trigger_condition, status) VALUES
('每月基础积分', '每月固定发放基础福利积分', 'FIXED', '500', '每月1日自动发放', 'ACTIVE'),
('工龄纪念奖励', '员工入职满周年特别奖励', 'OTHER', '1000', '入职满1/3/5/10年', 'ACTIVE'),
('入职欢迎积分', '新员工入职当天自动发放', 'ONBOARDING', '200', '入职当天自动触发', 'ACTIVE'),
('绩效优秀奖励', '季度绩效考核优秀员工奖励', 'PERFORMANCE', '200~800', '绩效考核A及以上', 'ACTIVE'),
('重要项目奖励', '参与公司重点项目完成奖励', 'PROJECT', '500', '项目完成后HR审批发放', 'ACTIVE'),
('推荐新员工奖励', '成功推荐新员工入职奖励', 'OTHER', '300', '被推荐人入职满3个月', 'DISABLED');
