package com.awsome.shop.point.facade.http.controller;

import com.awsome.shop.point.application.api.dto.point.*;
import com.awsome.shop.point.application.api.dto.point.request.*;
import com.awsome.shop.point.application.api.service.point.PointApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员积分端点
 */
@Tag(name = "积分-管理员", description = "管理员积分管理接口")
@RestController
@RequestMapping("/api/v1/point/admin")
@RequiredArgsConstructor
public class PointAdminController {

    private final PointApplicationService pointApplicationService;

    @Operation(summary = "查看所有员工积分余额")
    @GetMapping("/balances")
    public Result<PageResult<PointBalanceDTO>> listBalances(@Valid QueryBalancesRequest request) {
        return Result.success(pointApplicationService.listBalances(request));
    }

    @Operation(summary = "查看指定员工积分变动明细")
    @GetMapping("/transactions/{userId}")
    public Result<PageResult<PointTransactionDTO>> listUserTransactions(
            @PathVariable Long userId,
            @Valid QueryTransactionsRequest request) {
        return Result.success(pointApplicationService.listUserTransactions(userId, request));
    }

    @Operation(summary = "手动调整员工积分")
    @PostMapping("/adjust")
    public Result<PointTransactionDTO> adjustPoints(
            @RequestHeader("X-User-Id") Long operatorId,
            @RequestBody @Valid AdjustPointsRequest request) {
        return Result.success(pointApplicationService.adjustPoints(request, operatorId));
    }

    @Operation(summary = "获取发放配置")
    @GetMapping("/config")
    public Result<DistributionConfigDTO> getDistributionConfig() {
        return Result.success(pointApplicationService.getDistributionConfig());
    }

    @Operation(summary = "更新发放配置")
    @PutMapping("/config")
    public Result<DistributionConfigDTO> updateDistributionConfig(
            @RequestBody @Valid UpdateDistributionConfigRequest request) {
        return Result.success(pointApplicationService.updateDistributionConfig(request));
    }
}
