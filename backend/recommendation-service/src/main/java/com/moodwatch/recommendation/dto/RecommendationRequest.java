package com.moodwatch.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RecommendationRequest(
        @NotBlank String mood,
        Double minRating,
        Integer maxRuntime,
        List<String> genres,
        String era,
        boolean includeWatched
) {}
