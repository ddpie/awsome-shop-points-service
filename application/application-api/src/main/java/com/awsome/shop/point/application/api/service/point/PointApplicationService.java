package com.awsome.shop.point.application.api.service.point;

import com.awsome.shop.point.application.api.dto.point.*;
import com.awsome.shop.point.application.api.dto.point.request.*;
import com.awsome.shop.point.common.dto.PageResult;

/**
 * 积分应用服务接口
 */
public interface PointApplicationService {

    // ===== 员工端点 =====

    PointBalanceDTO getMyBalance(Long userId);

    PageResult<PointTransactionDTO> getMyTransactions(Long userId, QueryTransactionsRequest request);

    // ===== 管理员端点 =====

    PageResult<PointBalanceDTO> listBalances(QueryBalancesRequest request);

    PageResult<PointTransactionDTO> listUserTransactions(Long userId, QueryTransactionsRequest request);

    PointTransactionDTO adjustPoints(AdjustPointsRequest request, Long operatorId);

    DistributionConfigDTO getDistributionConfig();

    DistributionConfigDTO updateDistributionConfig(UpdateDistributionConfigRequest request);

    // ===== 内部端点 =====

    PointBalanceDTO initPoints(InitPointsRequest request);

    PointTransactionDTO deductPoints(DeductPointsRequest request);

    void rollbackDeduction(RollbackDeductionRequest request);

    PointBalanceDTO getBalanceByUserId(Long userId);
}
