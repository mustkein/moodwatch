package com.moodwatch.movie.dto;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        T data,
        String error,
        String message,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> fail(String error, String message) {
        return new ApiResponse<>(false, null, error, message, Instant.now());
    }
}
