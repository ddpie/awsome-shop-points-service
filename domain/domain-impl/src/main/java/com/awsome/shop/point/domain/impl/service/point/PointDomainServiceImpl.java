package com.awsome.shop.point.domain.impl.service.point;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.common.enums.PointsErrorCode;
import com.awsome.shop.point.common.exception.BusinessException;
import com.awsome.shop.point.domain.model.point.*;
import com.awsome.shop.point.domain.service.point.PointDomainService;
import com.awsome.shop.point.repository.point.PointBalanceRepository;
import com.awsome.shop.point.repository.point.PointTransactionRepository;
import com.awsome.shop.point.repository.point.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 积分领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointDomainServiceImpl implements PointDomainService {

    private static final String CONFIG_KEY_DISTRIBUTION_AMOUNT = "points.distribution.amount";
    private static final int DEFAULT_DISTRIBUTION_AMOUNT = 100;

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final SystemConfigRepository systemConfigRepository;

    @Override
    @Transactional
    public PointBalanceEntity initBalance(Long userId) {
        // 幂等：已存在则直接返回
        PointBalanceEntity existing = pointBalanceRepository.getByUserId(userId);
        if (existing != null) {
            return existing;
        }
        PointBalanceEntity entity = new PointBalanceEntity();
        entity.setUserId(userId);
        entity.setBalance(0);
        pointBalanceRepository.save(entity);
        return pointBalanceRepository.getByUserId(userId);
    }

    @Override
    public PointBalanceEntity getBalance(Long userId) {
        PointBalanceEntity entity = pointBalanceRepository.getByUserId(userId);
        if (entity == null) {
            throw new BusinessException(PointsErrorCode.BALANCE_NOT_FOUND);
        }
        return entity;
    }

    @Override
    public PageResult<PointTransactionEntity> getTransactions(Long userId, int page, int size, TransactionType type) {
        return pointTransactionRepository.pageByUserId(userId, page, size, type);
    }

    @Override
    public PageResult<PointBalanceEntity> pageBalances(int page, int size, Long userId) {
        return pointBalanceRepository.page(page, size, userId);
    }

    @Override
    @Transactional
    public PointTransactionEntity adjustPoints(Long userId, Integer amount, String remark, Long operatorId) {
        // 悲观锁查询余额
        PointBalanceEntity balance = pointBalanceRepository.getByUserIdForUpdate(userId);
        if (balance == null) {
            throw new BusinessException(PointsErrorCode.BALANCE_NOT_FOUND);
        }

        // 扣除场景校验余额
        if (amount < 0 && !balance.hasSufficientBalance(Math.abs(amount))) {
            throw new BusinessException(PointsErrorCode.INSUFFICIENT_BALANCE_ADJUST);
        }

        // 更新余额
        balance.addBalance(amount);
        pointBalanceRepository.updateBalance(userId, balance.getBalance());

        // 创建变动记录
        PointTransactionEntity transaction = new PointTransactionEntity();
        transaction.setUserId(userId);
        transaction.setType(amount > 0 ? TransactionType.MANUAL_ADD : TransactionType.MANUAL_DEDUCT);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balance.getBalance());
        transaction.setOperatorId(operatorId);
        transaction.setRemark(remark);
        pointTransactionRepository.save(transaction);
        return transaction;
    }

    @Override
    @Transactional
    public PointTransactionEntity deductPoints(Long userId, Integer amount, Long orderId) {
        // 悲观锁查询余额
        PointBalanceEntity balance = pointBalanceRepository.getByUserIdForUpdate(userId);
        if (balance == null) {
            throw new BusinessException(PointsErrorCode.BALANCE_NOT_FOUND);
        }

        // 余额校验
        if (!balance.hasSufficientBalance(amount)) {
            throw new BusinessException(PointsErrorCode.INSUFFICIENT_BALANCE_REDEEM);
        }

        // 扣除余额
        balance.deductBalance(amount);
        pointBalanceRepository.updateBalance(userId, balance.getBalance());

        // 创建变动记录
        PointTransactionEntity transaction = new PointTransactionEntity();
        transaction.setUserId(userId);
        transaction.setType(TransactionType.REDEMPTION);
        transaction.setAmount(-amount);
        transaction.setBalanceAfter(balance.getBalance());
        transaction.setReferenceId(orderId);
        transaction.setRemark("兑换扣除");
        pointTransactionRepository.save(transaction);
        return transaction;
    }

    @Override
    @Transactional
    public void rollbackDeduction(Long transactionId) {
        // 查询原始扣除记录
        PointTransactionEntity original = pointTransactionRepository.getById(transactionId);
        if (original == null) {
            throw new BusinessException(PointsErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (original.getType() != TransactionType.REDEMPTION) {
            throw new BusinessException(PointsErrorCode.INVALID_ROLLBACK_TYPE);
        }

        // 检查是否已回滚
        if (pointTransactionRepository.existsRollbackByReferenceId(original.getReferenceId())) {
            throw new BusinessException(PointsErrorCode.DUPLICATE_ROLLBACK);
        }

        // 恢复余额（悲观锁）
        int restoreAmount = Math.abs(original.getAmount());
        PointBalanceEntity balance = pointBalanceRepository.getByUserIdForUpdate(original.getUserId());
        balance.addBalance(restoreAmount);
        pointBalanceRepository.updateBalance(original.getUserId(), balance.getBalance());

        // 创建回滚记录
        PointTransactionEntity rollback = new PointTransactionEntity();
        rollback.setUserId(original.getUserId());
        rollback.setType(TransactionType.ROLLBACK);
        rollback.setAmount(restoreAmount);
        rollback.setBalanceAfter(balance.getBalance());
        rollback.setReferenceId(original.getReferenceId());
        rollback.setRemark("兑换回滚");
        pointTransactionRepository.save(rollback);
    }

    @Override
    public Integer getDistributionAmount() {
        SystemConfigEntity config = systemConfigRepository.getByKey(CONFIG_KEY_DISTRIBUTION_AMOUNT);
        if (config == null) {
            return DEFAULT_DISTRIBUTION_AMOUNT;
        }
        return Integer.parseInt(config.getConfigValue());
    }

    @Override
    public void updateDistributionAmount(Integer amount) {
        SystemConfigEntity config = new SystemConfigEntity();
        config.setConfigKey(CONFIG_KEY_DISTRIBUTION_AMOUNT);
        config.setConfigValue(String.valueOf(amount));
        config.setDescription("每月自动发放积分额度");
        systemConfigRepository.upsert(config);
    }

    @Override
    public List<PointBalanceEntity> findAllBalances() {
        return pointBalanceRepository.findAll();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void distributeToUser(Long userId, Integer amount, String remark) {
        PointBalanceEntity balance = pointBalanceRepository.getByUserIdForUpdate(userId);
        if (balance == null) {
            log.warn("用户 {} 积分余额记录不存在，跳过发放", userId);
            return;
        }
        balance.addBalance(amount);
        pointBalanceRepository.updateBalance(userId, balance.getBalance());

        PointTransactionEntity transaction = new PointTransactionEntity();
        transaction.setUserId(userId);
        transaction.setType(TransactionType.DISTRIBUTION);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balance.getBalance());
        transaction.setRemark(remark);
        pointTransactionRepository.save(transaction);
    }
}
