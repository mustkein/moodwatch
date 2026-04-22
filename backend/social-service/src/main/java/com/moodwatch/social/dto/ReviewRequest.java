package com.moodwatch.social.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull Long movieId,
        @NotNull @Min(1) @Max(10) Integer rating,
        String text,
        String moodTag
) {}
