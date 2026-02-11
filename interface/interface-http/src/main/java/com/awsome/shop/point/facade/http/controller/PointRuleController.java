package com.awsome.shop.point.facade.http.controller;

import com.awsome.shop.point.application.api.dto.pointrule.PointRuleDTO;
import com.awsome.shop.point.application.api.dto.pointrule.request.ListPointRuleRequest;
import com.awsome.shop.point.application.api.service.pointrule.PointRuleApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分规则管理 Controller
 */
@Tag(name = "PointRule", description = "积分规则管理")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PointRuleController {

    private final PointRuleApplicationService pointRuleApplicationService;

    @Operation(summary = "分页查询积分规则")
    @PostMapping("/admin/point-rule/list")
    public Result<PageResult<PointRuleDTO>> list(@RequestBody @Valid ListPointRuleRequest request) {
        return Result.success(pointRuleApplicationService.list(request));
    }
}
