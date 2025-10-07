package com.example.DATN.dto.room;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageDto<T>(
        int size,
        int number,
        long totalElements,
        int totalPages,
        List<T> content
) {
    public static <T> PageDto<T> of(Page<T> page) {
        return new PageDto<>(
                page.getSize(),
                page.getNumber(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent()
        );
    }
}
