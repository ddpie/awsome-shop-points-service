package com.awsome.shop.point.common.enums;

/**
 * 错误码接口
 *
 * <p>所有错误码枚举都应该实现这个接口。</p>
 * <p>错误码前缀决定 HTTP 状态码映射：</p>
 * <ul>
 *   <li>NOT_FOUND_ → 404</li>
 *   <li>CONFLICT_ → 409</li>
 *   <li>PARAM_ → 400</li>
 *   <li>BIZ_ → 200（业务异常）</li>
 *   <li>SYS_ → 500</li>
 * </ul>
 */
public interface ErrorCode {

    String getCode();

    String getMessage();
}
