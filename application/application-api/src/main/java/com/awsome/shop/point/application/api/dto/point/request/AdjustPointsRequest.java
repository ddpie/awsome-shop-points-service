package com.awsome.shop.point.application.api.dto.point.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员手动调整积分请求
 */
@Data
public class AdjustPointsRequest {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @NotNull(message = "调整数量不能为空")
    private Integer amount;

    @NotBlank(message = "备注不能为空")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
