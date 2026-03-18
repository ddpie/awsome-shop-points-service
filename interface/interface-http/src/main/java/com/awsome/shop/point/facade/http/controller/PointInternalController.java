package com.awsome.shop.point.facade.http.controller;

import com.awsome.shop.point.application.api.dto.point.PointBalanceDTO;
import com.awsome.shop.point.application.api.dto.point.PointTransactionDTO;
import com.awsome.shop.point.application.api.dto.point.request.*;
import com.awsome.shop.point.application.api.service.point.PointApplicationService;
import com.awsome.shop.point.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 内部端点（服务间调用，不经过 API 网关）
 */
@Tag(name = "积分-内部", description = "服务间内部调用接口")
@RestController
@RequestMapping("/api/v1/internal/point")
@RequiredArgsConstructor
public class PointInternalController {

    private final PointApplicationService pointApplicationService;

    @Operation(summary = "初始化用户积分余额")
    @PostMapping("/init")
    public Result<PointBalanceDTO> initPoints(@RequestBody @Valid InitPointsRequest request) {
        return Result.success(pointApplicationService.initPoints(request));
    }

    @Operation(summary = "兑换扣除积分")
    @PostMapping("/deduct")
    public Result<PointTransactionDTO> deductPoints(@RequestBody @Valid DeductPointsRequest request) {
        return Result.success(pointApplicationService.deductPoints(request));
    }

    @Operation(summary = "回滚积分扣除")
    @PostMapping("/rollback")
    public Result<Void> rollbackDeduction(@RequestBody @Valid RollbackDeductionRequest request) {
        pointApplicationService.rollbackDeduction(request);
        return Result.success();
    }

    @Operation(summary = "查询指定用户积分余额")
    @PostMapping("/balance")
    public Result<PointBalanceDTO> getBalanceByUserId(@RequestBody @Valid GetBalanceRequest request) {
        return Result.success(pointApplicationService.getBalanceByUserId(request.getUserId()));
    }
}
