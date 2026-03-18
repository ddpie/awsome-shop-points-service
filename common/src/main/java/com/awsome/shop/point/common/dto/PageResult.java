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

    private Long current;
    private Long size;
    private Long total;
    private Long pages;
    private List<T> records;

    public <R> PageResult<R> convert(Function<T, R> converter) {
        List<R> convertedRecords = records.stream()
                .map(converter)
                .collect(Collectors.toList());

        PageResult<R> result = new PageResult<>();
        result.setCurrent(this.current);
        result.setSize(this.size);
        result.setTotal(this.total);
        result.setPages(this.pages);
        result.setRecords(convertedRecords);
        return result;
    }
}
