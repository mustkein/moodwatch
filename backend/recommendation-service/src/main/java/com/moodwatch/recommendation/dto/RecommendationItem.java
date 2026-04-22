package com.moodwatch.recommendation.dto;

public record RecommendationItem(
        String title,
        String reason,
        Double rating,
        String year
) {}
