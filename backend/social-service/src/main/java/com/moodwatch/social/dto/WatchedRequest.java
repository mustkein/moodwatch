package com.moodwatch.social.dto;

import jakarta.validation.constraints.NotNull;

public record WatchedRequest(
        @NotNull Long tmdbId,
        String title,
        Double rating
) {}
