package com.awsome.shop.point.facade.http.controller;

import com.awsome.shop.point.application.api.dto.point.PointBalanceDTO;
import com.awsome.shop.point.application.api.dto.point.PointTransactionDTO;
import com.awsome.shop.point.application.api.dto.point.request.QueryTransactionsRequest;
import com.awsome.shop.point.application.api.service.point.PointApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 员工积分端点
 *
 * <p>当前用户 ID 从请求头 X-User-Id（API 网关注入）获取</p>
 */
@Tag(name = "积分-员工", description = "员工积分查询接口")
@RestController
@RequestMapping("/api/v1/point")
@RequiredArgsConstructor
public class PointController {

    private final PointApplicationService pointApplicationService;

    @Operation(summary = "查询当前用户积分余额")
    @GetMapping("/balance")
    public Result<PointBalanceDTO> getMyBalance(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(pointApplicationService.getMyBalance(userId));
    }

    @Operation(summary = "查询当前用户积分变动历史")
    @GetMapping("/transactions")
    public Result<PageResult<PointTransactionDTO>> getMyTransactions(
            @RequestHeader("X-User-Id") Long userId,
            @Valid QueryTransactionsRequest request) {
        return Result.success(pointApplicationService.getMyTransactions(userId, request));
    }
}
