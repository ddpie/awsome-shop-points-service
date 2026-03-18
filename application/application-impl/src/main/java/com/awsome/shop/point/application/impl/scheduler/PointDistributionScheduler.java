package com.awsome.shop.point.application.impl.scheduler;

import com.awsome.shop.point.domain.model.point.PointBalanceEntity;
import com.awsome.shop.point.domain.service.point.PointDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 积分自动发放定时任务
 *
 * <p>每月1日凌晨2:00执行，为所有已有积分余额记录的用户发放积分</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointDistributionScheduler {

    private final PointDomainService pointDomainService;

    @Scheduled(cron = "0 0 2 1 * ?")
    public void distributeMonthlyPoints() {
        log.info("[积分发放] 开始执行每月积分自动发放");

        Integer amount = pointDomainService.getDistributionAmount();
        List<PointBalanceEntity> allBalances = pointDomainService.findAllBalances();

        String monthLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月"));
        String remark = "系统自动发放 - " + monthLabel;

        int total = allBalances.size();
        int success = 0;
        int failed = 0;

        for (PointBalanceEntity balance : allBalances) {
            try {
                pointDomainService.distributeToUser(balance.getUserId(), amount, remark);
                success++;
            } catch (Exception e) {
                failed++;
                log.error("[积分发放] 用户 {} 发放失败", balance.getUserId(), e);
            }
        }

        log.info("[积分发放] 发放完成: 总人数={}, 成功={}, 失败={}, 每人发放={}", total, success, failed, amount);
    }
}
