package com.kaua.file.processor.domain.pagination;

public record PaginationMetadata(
        int currentPage,
        int perPage,
        int totalPages,
        long totalItems
) {
}
