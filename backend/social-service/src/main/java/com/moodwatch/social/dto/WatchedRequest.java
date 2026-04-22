package com.moodwatch.social.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WatchedRequest(
        @NotNull Long movieId,
        @Min(1) @Max(10) Integer rating
) {}
