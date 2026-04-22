package com.moodwatch.recommendation.dto;

import java.util.List;

public record RecommendationResponse(
        List<RecommendationItem> items
) {}
