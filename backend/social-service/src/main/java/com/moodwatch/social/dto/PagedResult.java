package com.moodwatch.social.dto;

import java.util.List;

public record PagedResult<T>(
        List<T> items,
        long totalElements,
        int totalPages,
        int currentPage
) {}
