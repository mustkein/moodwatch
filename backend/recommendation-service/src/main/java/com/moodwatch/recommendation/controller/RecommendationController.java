package com.moodwatch.recommendation.controller;

import com.moodwatch.recommendation.dto.ApiResponse;
import com.moodwatch.recommendation.dto.RecommendationRequest;
import com.moodwatch.recommendation.dto.RecommendationResponse;
import com.moodwatch.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/mood")
    public ResponseEntity<ApiResponse<RecommendationResponse>> recommend(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody RecommendationRequest request) {
        System.err.println("[Rec] request received: " + request);
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.recommend(userId, request)));
    }
}
