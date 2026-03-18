package com.awsome.shop.point.application.api.dto.point;

import lombok.Data;

/**
 * 积分余额 DTO
 */
@Data
public class PointBalanceDTO {

    private Long userId;
    private Integer balance;
}
