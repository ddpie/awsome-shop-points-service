package com.awsome.shop.point.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页结果类
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long currentPage;
    private Long totalElements;
    private Long totalPages;
    private List<T> content;

    public <R> PageResult<R> convert(Function<T, R> converter) {
        List<R> convertedRecords = content.stream()
                .map(converter)
                .collect(Collectors.toList());

        PageResult<R> result = new PageResult<>();
        result.setCurrentPage(this.currentPage);
        result.setTotalElements(this.totalElements);
        result.setTotalPages(this.totalPages);
        result.setContent(convertedRecords);
        return result;
    }
}
