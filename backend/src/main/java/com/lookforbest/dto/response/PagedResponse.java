package com.lookforbest.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long total;
    private int totalPages;
    private boolean hasNext;

    public static <T> PagedResponse<T> of(Page<T> pageData) {
        return PagedResponse.<T>builder()
                .content(pageData.getContent())
                .page(pageData.getNumber())
                .size(pageData.getSize())
                .total(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .hasNext(pageData.hasNext())
                .build();
    }
}
