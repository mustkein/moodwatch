package com.moodwatch.user.dto;

public record RefreshResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
