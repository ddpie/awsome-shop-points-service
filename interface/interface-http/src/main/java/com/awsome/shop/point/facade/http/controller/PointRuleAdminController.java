package com.awsome.shop.point.facade.http.controller;

import com.awsome.shop.point.application.api.dto.point.PointsRuleDTO;
import com.awsome.shop.point.application.api.dto.point.request.CreateRuleRequest;
import com.awsome.shop.point.application.api.dto.point.request.UpdateRuleRequest;
import com.awsome.shop.point.application.api.dto.point.request.UpdateRuleStatusRequest;
import com.awsome.shop.point.application.api.service.point.RuleApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "积分规则-管理员", description = "积分规则管理接口")
@RestController
@RequestMapping("/api/v1/point/admin/rules")
@RequiredArgsConstructor
public class PointRuleAdminController {

    private final RuleApplicationService ruleApplicationService;

    @Operation(summary = "规则列表（分页）")
    @GetMapping
    public Result<PageResult<PointsRuleDTO>> listRules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(ruleApplicationService.listRules(page, size));
    }

    @Operation(summary = "新增规则")
    @PostMapping
    public Result<PointsRuleDTO> createRule(@RequestBody @Valid CreateRuleRequest request) {
        return Result.success(ruleApplicationService.createRule(request));
    }

    @Operation(summary = "编辑规则")
    @PutMapping("/{id}")
    public Result<PointsRuleDTO> updateRule(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRuleRequest request) {
        return Result.success(ruleApplicationService.updateRule(id, request));
    }

    @Operation(summary = "启用/禁用规则")
    @PutMapping("/{id}/status")
    public Result<PointsRuleDTO> updateRuleStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRuleStatusRequest request) {
        return Result.success(ruleApplicationService.updateRuleStatus(id, request.getStatus()));
    }

    @Operation(summary = "删除规则")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        ruleApplicationService.deleteRule(id);
        return Result.success();
    }
}
