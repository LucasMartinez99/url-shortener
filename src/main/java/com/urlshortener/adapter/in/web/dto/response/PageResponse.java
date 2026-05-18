package com.urlshortener.adapter.in.web.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wraps Spring's Page<T> into a serializable JSON response.
 * Avoids leaking Spring Data types to the API contract.
 */
public record PageResponse<T>(
        List<T> content,
        int     page,
        int     size,
        long    totalElements,
        int     totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
