package com.awsome.shop.point.domain.impl.service.point;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.common.enums.PointsErrorCode;
import com.awsome.shop.point.common.exception.BusinessException;
import com.awsome.shop.point.domain.model.point.*;
import com.awsome.shop.point.repository.point.PointBalanceRepository;
import com.awsome.shop.point.repository.point.PointTransactionRepository;
import com.awsome.shop.point.repository.point.SystemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointDomainServiceImplTest {

    @Mock
    private PointBalanceRepository pointBalanceRepository;
    @Mock
    private PointTransactionRepository pointTransactionRepository;
    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private PointDomainServiceImpl pointDomainService;

    private PointBalanceEntity buildBalance(Long userId, Integer balance) {
        PointBalanceEntity entity = new PointBalanceEntity();
        entity.setId(1L);
        entity.setUserId(userId);
        entity.setBalance(balance);
        return entity;
    }

    private PointTransactionEntity buildRedemptionTransaction(Long id, Long userId, Integer amount, Long referenceId) {
        PointTransactionEntity entity = new PointTransactionEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setType(TransactionType.REDEMPTION);
        entity.setAmount(amount);
        entity.setReferenceId(referenceId);
        return entity;
    }

    // ===== initBalance =====

    @Nested
    @DisplayName("initBalance - 积分余额初始化")
    class InitBalanceTests {

        @Test
        @DisplayName("BR-002: 已存在余额记录时直接返回（幂等）")
        void shouldReturnExistingBalance() {
            PointBalanceEntity existing = buildBalance(1L, 50);
            when(pointBalanceRepository.getByUserId(1L)).thenReturn(existing);

            PointBalanceEntity result = pointDomainService.initBalance(1L);

            assertThat(result).isSameAs(existing);
            verify(pointBalanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("BR-002: 不存在时创建初始余额为0的记录")
        void shouldCreateNewBalanceWithZero() {
            when(pointBalanceRepository.getByUserId(1L)).thenReturn(null).thenReturn(buildBalance(1L, 0));

            PointBalanceEntity result = pointDomainService.initBalance(1L);

            ArgumentCaptor<PointBalanceEntity> captor = ArgumentCaptor.forClass(PointBalanceEntity.class);
            verify(pointBalanceRepository).save(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(1L);
            assertThat(captor.getValue().getBalance()).isEqualTo(0);
            assertThat(result.getBalance()).isEqualTo(0);
        }
    }

    // ===== getBalance =====

    @Nested
    @DisplayName("getBalance - 查询积分余额")
    class GetBalanceTests {

        @Test
        @DisplayName("余额存在时正常返回")
        void shouldReturnBalance() {
            PointBalanceEntity balance = buildBalance(1L, 200);
            when(pointBalanceRepository.getByUserId(1L)).thenReturn(balance);

            assertThat(pointDomainService.getBalance(1L)).isSameAs(balance);
        }

        @Test
        @DisplayName("余额不存在时抛出 BALANCE_NOT_FOUND (POINTS_001)")
        void shouldThrowWhenNotFound() {
            when(pointBalanceRepository.getByUserId(1L)).thenReturn(null);

            assertThatThrownBy(() -> pointDomainService.getBalance(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.BALANCE_NOT_FOUND.getCode());
        }
    }

    // ===== getTransactions =====

    @Nested
    @DisplayName("getTransactions - 查询积分变动历史")
    class GetTransactionsTests {

        @Test
        @DisplayName("按 userId 分页查询变动历史")
        void shouldReturnPagedTransactions() {
            PageResult<PointTransactionEntity> mockPage = new PageResult<>();
            mockPage.setCurrent(1L);
            mockPage.setSize(20L);
            mockPage.setTotal(1L);
            mockPage.setPages(1L);
            PointTransactionEntity tx = new PointTransactionEntity();
            tx.setId(1L);
            tx.setUserId(1L);
            tx.setType(TransactionType.DISTRIBUTION);
            tx.setAmount(100);
            mockPage.setRecords(Collections.singletonList(tx));

            when(pointTransactionRepository.pageByUserId(1L, 1, 20, null)).thenReturn(mockPage);

            PageResult<PointTransactionEntity> result = pointDomainService.getTransactions(1L, 1, 20, null);

            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getTotal()).isEqualTo(1L);
            verify(pointTransactionRepository).pageByUserId(1L, 1, 20, null);
        }

        @Test
        @DisplayName("按类型筛选变动历史")
        void shouldFilterByType() {
            PageResult<PointTransactionEntity> mockPage = new PageResult<>();
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0L);
            mockPage.setCurrent(1L);
            mockPage.setSize(20L);
            mockPage.setPages(0L);

            when(pointTransactionRepository.pageByUserId(1L, 1, 20, TransactionType.REDEMPTION)).thenReturn(mockPage);

            PageResult<PointTransactionEntity> result = pointDomainService.getTransactions(1L, 1, 20, TransactionType.REDEMPTION);

            assertThat(result.getRecords()).isEmpty();
            verify(pointTransactionRepository).pageByUserId(1L, 1, 20, TransactionType.REDEMPTION);
        }
    }

    // ===== pageBalances =====

    @Nested
    @DisplayName("pageBalances - 管理员查看所有员工积分余额")
    class PageBalancesTests {

        @Test
        @DisplayName("分页查询所有余额")
        void shouldReturnPagedBalances() {
            PageResult<PointBalanceEntity> mockPage = new PageResult<>();
            mockPage.setCurrent(1L);
            mockPage.setSize(20L);
            mockPage.setTotal(2L);
            mockPage.setPages(1L);
            mockPage.setRecords(Arrays.asList(buildBalance(1L, 100), buildBalance(2L, 200)));

            when(pointBalanceRepository.page(1, 20, null)).thenReturn(mockPage);

            PageResult<PointBalanceEntity> result = pointDomainService.pageBalances(1, 20, null);

            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(2L);
        }

        @Test
        @DisplayName("按 userId 精确匹配查询")
        void shouldFilterByUserId() {
            PageResult<PointBalanceEntity> mockPage = new PageResult<>();
            mockPage.setCurrent(1L);
            mockPage.setSize(20L);
            mockPage.setTotal(1L);
            mockPage.setPages(1L);
            mockPage.setRecords(Collections.singletonList(buildBalance(1L, 100)));

            when(pointBalanceRepository.page(1, 20, 1L)).thenReturn(mockPage);

            PageResult<PointBalanceEntity> result = pointDomainService.pageBalances(1, 20, 1L);

            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getUserId()).isEqualTo(1L);
        }
    }

    // ===== adjustPoints =====

    @Nested
    @DisplayName("adjustPoints - 管理员手动调整积分")
    class AdjustPointsTests {

        @Test
        @DisplayName("BR-010: 增加积分成功，type=MANUAL_ADD，amount > 0")
        void shouldAddPoints() {
            PointBalanceEntity balance = buildBalance(1L, 100);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            PointTransactionEntity result = pointDomainService.adjustPoints(1L, 50, "奖励", 99L);

            verify(pointBalanceRepository).updateBalance(1L, 150);
            ArgumentCaptor<PointTransactionEntity> captor = ArgumentCaptor.forClass(PointTransactionEntity.class);
            verify(pointTransactionRepository).save(captor.capture());
            PointTransactionEntity saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo(TransactionType.MANUAL_ADD);
            assertThat(saved.getAmount()).isEqualTo(50);
            assertThat(saved.getBalanceAfter()).isEqualTo(150);
            assertThat(saved.getOperatorId()).isEqualTo(99L);
            assertThat(saved.getRemark()).isEqualTo("奖励");
            assertThat(saved.getUserId()).isEqualTo(1L);
            // BR-003: 余额更新和流水创建在同一调用中
            assertThat(result.getType()).isEqualTo(TransactionType.MANUAL_ADD);
        }

        @Test
        @DisplayName("BR-010: 扣除积分成功，type=MANUAL_DEDUCT，amount < 0")
        void shouldDeductPoints() {
            PointBalanceEntity balance = buildBalance(1L, 100);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            PointTransactionEntity result = pointDomainService.adjustPoints(1L, -30, "扣除", 99L);

            verify(pointBalanceRepository).updateBalance(1L, 70);
            assertThat(result.getType()).isEqualTo(TransactionType.MANUAL_DEDUCT);
            assertThat(result.getAmount()).isEqualTo(-30);
            assertThat(result.getBalanceAfter()).isEqualTo(70);
        }

        @Test
        @DisplayName("BR-001: 扣除后余额恰好为0时成功")
        void shouldDeductToZero() {
            PointBalanceEntity balance = buildBalance(1L, 50);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            PointTransactionEntity result = pointDomainService.adjustPoints(1L, -50, "清零", 99L);

            verify(pointBalanceRepository).updateBalance(1L, 0);
            assertThat(result.getBalanceAfter()).isEqualTo(0);
            assertThat(result.getType()).isEqualTo(TransactionType.MANUAL_DEDUCT);
        }

        @Test
        @DisplayName("BR-001: 扣除后余额不足时抛出 INSUFFICIENT_BALANCE_ADJUST (POINTS_002)")
        void shouldThrowWhenInsufficientBalance() {
            PointBalanceEntity balance = buildBalance(1L, 20);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            assertThatThrownBy(() -> pointDomainService.adjustPoints(1L, -50, "扣除", 99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.INSUFFICIENT_BALANCE_ADJUST.getCode());

            verify(pointBalanceRepository, never()).updateBalance(anyLong(), anyInt());
            verify(pointTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("BR-004: 用户余额不存在时抛出 BALANCE_NOT_FOUND (POINTS_001)")
        void shouldThrowWhenBalanceNotFound() {
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(null);

            assertThatThrownBy(() -> pointDomainService.adjustPoints(1L, 50, "奖励", 99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.BALANCE_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("BR-007: 验证 operatorId 和 remark 被正确记录（审计）")
        void shouldRecordOperatorAndRemark() {
            PointBalanceEntity balance = buildBalance(1L, 100);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            pointDomainService.adjustPoints(1L, 10, "测试备注内容", 88L);

            ArgumentCaptor<PointTransactionEntity> captor = ArgumentCaptor.forClass(PointTransactionEntity.class);
            verify(pointTransactionRepository).save(captor.capture());
            assertThat(captor.getValue().getOperatorId()).isEqualTo(88L);
            assertThat(captor.getValue().getRemark()).isEqualTo("测试备注内容");
        }
    }

    // ===== deductPoints =====

    @Nested
    @DisplayName("deductPoints - 兑换扣除积分")
    class DeductPointsTests {

        @Test
        @DisplayName("BR-010: 扣除成功，type=REDEMPTION，amount 存储为负数")
        void shouldDeductSuccessfully() {
            PointBalanceEntity balance = buildBalance(1L, 200);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            PointTransactionEntity result = pointDomainService.deductPoints(1L, 80, 1001L);

            verify(pointBalanceRepository).updateBalance(1L, 120);
            ArgumentCaptor<PointTransactionEntity> captor = ArgumentCaptor.forClass(PointTransactionEntity.class);
            verify(pointTransactionRepository).save(captor.capture());
            PointTransactionEntity saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo(TransactionType.REDEMPTION);
            assertThat(saved.getAmount()).isEqualTo(-80); // BR-010: REDEMPTION amount < 0
            assertThat(saved.getBalanceAfter()).isEqualTo(120);
            assertThat(saved.getReferenceId()).isEqualTo(1001L);
            assertThat(saved.getRemark()).isEqualTo("兑换扣除");
            assertThat(saved.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("BR-001: 余额不足时抛出 INSUFFICIENT_BALANCE_REDEEM (POINTS_003)")
        void shouldThrowWhenInsufficientBalance() {
            PointBalanceEntity balance = buildBalance(1L, 50);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            assertThatThrownBy(() -> pointDomainService.deductPoints(1L, 100, 1001L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.INSUFFICIENT_BALANCE_REDEEM.getCode());

            verify(pointBalanceRepository, never()).updateBalance(anyLong(), anyInt());
            verify(pointTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("余额记录不存在时抛出 BALANCE_NOT_FOUND (POINTS_001)")
        void shouldThrowWhenBalanceNotFound() {
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(null);

            assertThatThrownBy(() -> pointDomainService.deductPoints(1L, 80, 1001L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.BALANCE_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("BR-001: 扣除后余额恰好为0时成功")
        void shouldDeductToZero() {
            PointBalanceEntity balance = buildBalance(1L, 100);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            PointTransactionEntity result = pointDomainService.deductPoints(1L, 100, 1001L);

            verify(pointBalanceRepository).updateBalance(1L, 0);
            assertThat(result.getBalanceAfter()).isEqualTo(0);
            assertThat(result.getAmount()).isEqualTo(-100);
        }

        @Test
        @DisplayName("BR-003: 验证流水 balanceAfter 等于变动后实际余额")
        void shouldRecordCorrectBalanceAfter() {
            PointBalanceEntity balance = buildBalance(1L, 300);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            PointTransactionEntity result = pointDomainService.deductPoints(1L, 150, 2001L);

            assertThat(result.getBalanceAfter()).isEqualTo(150);
            verify(pointBalanceRepository).updateBalance(1L, 150);
        }
    }

    // ===== rollbackDeduction =====

    @Nested
    @DisplayName("rollbackDeduction - 兑换回滚")
    class RollbackDeductionTests {

        @Test
        @DisplayName("BR-005/BR-006: 回滚成功，type=ROLLBACK，amount 为正数")
        void shouldRollbackSuccessfully() {
            PointTransactionEntity original = buildRedemptionTransaction(10L, 1L, -80, 1001L);
            PointBalanceEntity balance = buildBalance(1L, 120);

            when(pointTransactionRepository.getById(10L)).thenReturn(original);
            when(pointTransactionRepository.existsRollbackByReferenceId(1001L)).thenReturn(false);
            when(pointBalanceRepository.getByUserIdForUpdate(1L)).thenReturn(balance);

            pointDomainService.rollbackDeduction(10L);

            verify(pointBalanceRepository).updateBalance(1L, 200); // 120 + 80
            ArgumentCaptor<PointTransactionEntity> captor = ArgumentCaptor.forClass(PointTransactionEntity.class);
            verify(pointTransactionRepository).save(captor.capture());
            PointTransactionEntity rollback = captor.getValue();
            assertThat(rollback.getType()).isEqualTo(TransactionType.ROLLBACK);
            assertThat(rollback.getAmount()).isEqualTo(80); // BR-010: ROLLBACK amount > 0
            assertThat(rollback.getBalanceAfter()).isEqualTo(200);
            assertThat(rollback.getReferenceId()).isEqualTo(1001L); // 关联原始订单ID
            assertThat(rollback.getRemark()).isEqualTo("兑换回滚");
            assertThat(rollback.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("变动记录不存在时抛出 TRANSACTION_NOT_FOUND (POINTS_004)")
        void shouldThrowWhenTransactionNotFound() {
            when(pointTransactionRepository.getById(10L)).thenReturn(null);

            assertThatThrownBy(() -> pointDomainService.rollbackDeduction(10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.TRANSACTION_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("BR-006: 非兑换扣除记录(MANUAL_DEDUCT)不允许回滚 (POINTS_005)")
        void shouldThrowWhenNotRedemption_ManualDeduct() {
            PointTransactionEntity original = new PointTransactionEntity();
            original.setId(10L);
            original.setType(TransactionType.MANUAL_DEDUCT);
            when(pointTransactionRepository.getById(10L)).thenReturn(original);

            assertThatThrownBy(() -> pointDomainService.rollbackDeduction(10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.INVALID_ROLLBACK_TYPE.getCode());
        }

        @Test
        @DisplayName("BR-006: 非兑换扣除记录(DISTRIBUTION)不允许回滚 (POINTS_005)")
        void shouldThrowWhenNotRedemption_Distribution() {
            PointTransactionEntity original = new PointTransactionEntity();
            original.setId(11L);
            original.setType(TransactionType.DISTRIBUTION);
            when(pointTransactionRepository.getById(11L)).thenReturn(original);

            assertThatThrownBy(() -> pointDomainService.rollbackDeduction(11L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.INVALID_ROLLBACK_TYPE.getCode());
        }

        @Test
        @DisplayName("BR-006: 非兑换扣除记录(MANUAL_ADD)不允许回滚 (POINTS_005)")
        void shouldThrowWhenNotRedemption_ManualAdd() {
            PointTransactionEntity original = new PointTransactionEntity();
            original.setId(12L);
            original.setType(TransactionType.MANUAL_ADD);
            when(pointTransactionRepository.getById(12L)).thenReturn(original);

            assertThatThrownBy(() -> pointDomainService.rollbackDeduction(12L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.INVALID_ROLLBACK_TYPE.getCode());
        }

        @Test
        @DisplayName("BR-006: 非兑换扣除记录(ROLLBACK)不允许回滚 (POINTS_005)")
        void shouldThrowWhenNotRedemption_Rollback() {
            PointTransactionEntity original = new PointTransactionEntity();
            original.setId(13L);
            original.setType(TransactionType.ROLLBACK);
            when(pointTransactionRepository.getById(13L)).thenReturn(original);

            assertThatThrownBy(() -> pointDomainService.rollbackDeduction(13L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.INVALID_ROLLBACK_TYPE.getCode());
        }

        @Test
        @DisplayName("BR-005: 重复回滚时抛出 DUPLICATE_ROLLBACK (POINTS_006)")
        void shouldThrowWhenAlreadyRolledBack() {
            PointTransactionEntity original = buildRedemptionTransaction(10L, 1L, -80, 1001L);
            when(pointTransactionRepository.getById(10L)).thenReturn(original);
            when(pointTransactionRepository.existsRollbackByReferenceId(1001L)).thenReturn(true);

            assertThatThrownBy(() -> pointDomainService.rollbackDeduction(10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PointsErrorCode.DUPLICATE_ROLLBACK.getCode());

            verify(pointBalanceRepository, never()).getByUserIdForUpdate(anyLong());
            verify(pointBalanceRepository, never()).updateBalance(anyLong(), anyInt());
        }
    }

    // ===== getDistributionAmount =====

    @Nested
    @DisplayName("getDistributionAmount - 获取发放配置")
    class GetDistributionAmountTests {

        @Test
        @DisplayName("配置存在时返回配置值")
        void shouldReturnConfigValue() {
            SystemConfigEntity config = new SystemConfigEntity();
            config.setConfigValue("200");
            when(systemConfigRepository.getByKey("points.distribution.amount")).thenReturn(config);

            assertThat(pointDomainService.getDistributionAmount()).isEqualTo(200);
        }

        @Test
        @DisplayName("BR-009: 配置不存在时返回默认值100")
        void shouldReturnDefaultWhenNotFound() {
            when(systemConfigRepository.getByKey("points.distribution.amount")).thenReturn(null);

            assertThat(pointDomainService.getDistributionAmount()).isEqualTo(100);
        }
    }

    // ===== updateDistributionAmount =====

    @Nested
    @DisplayName("updateDistributionAmount - 更新发放配置")
    class UpdateDistributionAmountTests {

        @Test
        @DisplayName("更新发放配置，验证 configKey、configValue、description")
        void shouldUpdateDistributionAmount() {
            pointDomainService.updateDistributionAmount(300);

            ArgumentCaptor<SystemConfigEntity> captor = ArgumentCaptor.forClass(SystemConfigEntity.class);
            verify(systemConfigRepository).upsert(captor.capture());
            SystemConfigEntity config = captor.getValue();
            assertThat(config.getConfigKey()).isEqualTo("points.distribution.amount");
            assertThat(config.getConfigValue()).isEqualTo("300");
            assertThat(config.getDescription()).isEqualTo("每月自动发放积分额度");
        }
    }

    // ===== findAllBalances =====

    @Nested
    @DisplayName("findAllBalances - 查询所有余额记录")
    class FindAllBalancesTests {

        @Test
        @DisplayName("返回所有余额记录（用于定时发放）")
        void shouldReturnAllBalances() {
            List<PointBalanceEntity> balances = Arrays.asList(
                    buildBalance(1L, 100),
                    buildBalance(2L, 200)
            );
            when(pointBalanceRepository.findAll()).thenReturn(balances);

            List<PointBalanceEntity> result = pointDomainService.findAllBalances();

            assertThat(result).hasSize(2);
            verify(pointBalanceRepository).findAll();
        }

        @Test
        @DisplayName("无余额记录时返回空列表")
        void shouldReturnEmptyList() {
            when(pointBalanceRepository.findAll()).thenReturn(Collections.emptyList());

            List<PointBalanceEntity> result = pointDomainService.findAllBalances();

            assertThat(result).isEmpty();
        }
    }

    // ===== distributeToUser =====

    @Nested
    @DisplayName("distributeToUser - 为单个用户发放积分")
    class DistributeToUserTests {

        @Test
        @DisplayName("BR-008/BR-010: 发放成功，使用原子UPDATE，type=DISTRIBUTION，amount > 0")
        void shouldDistributeSuccessfully() {
            when(pointBalanceRepository.addBalanceAtomic(1L, 50)).thenReturn(150);

            pointDomainService.distributeToUser(1L, 50, "系统自动发放 - 2026年03月");

            verify(pointBalanceRepository).addBalanceAtomic(1L, 50);
            verify(pointBalanceRepository, never()).getByUserIdForUpdate(anyLong());
            ArgumentCaptor<PointTransactionEntity> captor = ArgumentCaptor.forClass(PointTransactionEntity.class);
            verify(pointTransactionRepository).save(captor.capture());
            PointTransactionEntity tx = captor.getValue();
            assertThat(tx.getType()).isEqualTo(TransactionType.DISTRIBUTION);
            assertThat(tx.getAmount()).isEqualTo(50);
            assertThat(tx.getBalanceAfter()).isEqualTo(150);
            assertThat(tx.getRemark()).isEqualTo("系统自动发放 - 2026年03月");
            assertThat(tx.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("余额记录不存在时跳过发放（不抛异常）")
        void shouldSkipWhenBalanceNotFound() {
            when(pointBalanceRepository.addBalanceAtomic(1L, 50)).thenReturn(null);

            pointDomainService.distributeToUser(1L, 50, "系统自动发放");

            verify(pointTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("BR-003/BR-004: 发放使用原子UPDATE而非悲观锁，余额更新与流水同时创建")
        void shouldUseAtomicUpdateNotPessimisticLock() {
            when(pointBalanceRepository.addBalanceAtomic(2L, 100)).thenReturn(100);

            pointDomainService.distributeToUser(2L, 100, "系统自动发放 - 2026年01月");

            verify(pointBalanceRepository).addBalanceAtomic(2L, 100);
            verify(pointBalanceRepository, never()).getByUserIdForUpdate(anyLong());
            verify(pointTransactionRepository).save(any(PointTransactionEntity.class));
        }
    }
}
