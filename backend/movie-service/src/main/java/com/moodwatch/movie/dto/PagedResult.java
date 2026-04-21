package com.moodwatch.movie.dto;

import java.util.List;

public record PagedResult<T>(
        List<T> items,
        int page,
        int totalPages,
        long totalItems
) {}
