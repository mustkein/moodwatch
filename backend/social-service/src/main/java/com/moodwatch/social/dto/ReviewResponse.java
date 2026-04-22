package com.moodwatch.social.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID userId,
        Long movieId,
        Integer rating,
        String text,
        String moodTag,
        Instant createdAt
) {}
