package com.moodwatch.recommendation.dto;

import java.util.List;

public record PagedResult<T>(
        List<T> items,
        long totalElements,
        int totalPages,
        int currentPage
) {}
