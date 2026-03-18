package com.awsome.shop.point.application.impl.service.point;

import com.awsome.shop.point.application.api.dto.point.*;
import com.awsome.shop.point.application.api.dto.point.request.*;
import com.awsome.shop.point.application.api.service.point.PointApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.point.*;
import com.awsome.shop.point.domain.service.point.PointDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 积分应用服务实现
 */
@Service
@RequiredArgsConstructor
public class PointApplicationServiceImpl implements PointApplicationService {

    private final PointDomainService pointDomainService;

    @Override
    public PointBalanceDTO getMyBalance(Long userId) {
        return toBalanceDTO(pointDomainService.getBalance(userId));
    }

    @Override
    public PageResult<PointTransactionDTO> getMyTransactions(Long userId, QueryTransactionsRequest request) {
        TransactionType type = parseType(request.getType());
        PageResult<PointTransactionEntity> page = pointDomainService.getTransactions(
                userId, request.getPage(), request.getSize(), type);
        return page.convert(this::toTransactionDTO);
    }

    @Override
    public PageResult<PointBalanceDTO> listBalances(QueryBalancesRequest request) {
        PageResult<PointBalanceEntity> page = pointDomainService.pageBalances(
                request.getPage(), request.getSize(), request.getKeyword());
        return page.convert(this::toBalanceDTO);
    }

    @Override
    public PageResult<PointTransactionDTO> listUserTransactions(Long userId, QueryTransactionsRequest request) {
        TransactionType type = parseType(request.getType());
        PageResult<PointTransactionEntity> page = pointDomainService.getTransactions(
                userId, request.getPage(), request.getSize(), type);
        return page.convert(this::toTransactionDTO);
    }

    @Override
    public PointTransactionDTO adjustPoints(AdjustPointsRequest request, Long operatorId) {
        PointTransactionEntity entity = pointDomainService.adjustPoints(
                request.getUserId(), request.getAmount(), request.getRemark(), operatorId);
        return toTransactionDTO(entity);
    }

    @Override
    public DistributionConfigDTO getDistributionConfig() {
        Integer amount = pointDomainService.getDistributionAmount();
        DistributionConfigDTO dto = new DistributionConfigDTO();
        dto.setAmount(amount);
        return dto;
    }

    @Override
    public DistributionConfigDTO updateDistributionConfig(UpdateDistributionConfigRequest request) {
        pointDomainService.updateDistributionAmount(request.getAmount());
        return getDistributionConfig();
    }

    @Override
    public PointBalanceDTO initPoints(InitPointsRequest request) {
        return toBalanceDTO(pointDomainService.initBalance(request.getUserId()));
    }

    @Override
    public PointTransactionDTO deductPoints(DeductPointsRequest request) {
        PointTransactionEntity entity = pointDomainService.deductPoints(
                request.getUserId(), request.getAmount(), request.getOrderId());
        return toTransactionDTO(entity);
    }

    @Override
    public void rollbackDeduction(RollbackDeductionRequest request) {
        pointDomainService.rollbackDeduction(request.getTransactionId());
    }

    @Override
    public PointBalanceDTO getBalanceByUserId(Long userId) {
        return toBalanceDTO(pointDomainService.getBalance(userId));
    }

    // ===== 转换方法 =====

    private PointBalanceDTO toBalanceDTO(PointBalanceEntity entity) {
        PointBalanceDTO dto = new PointBalanceDTO();
        dto.setUserId(entity.getUserId());
        dto.setBalance(entity.getBalance());
        return dto;
    }

    private PointTransactionDTO toTransactionDTO(PointTransactionEntity entity) {
        PointTransactionDTO dto = new PointTransactionDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setType(entity.getType().name());
        dto.setAmount(entity.getAmount());
        dto.setBalanceAfter(entity.getBalanceAfter());
        dto.setReferenceId(entity.getReferenceId());
        dto.setOperatorId(entity.getOperatorId());
        dto.setRemark(entity.getRemark());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private TransactionType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return TransactionType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
