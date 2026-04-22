package com.moodwatch.social.controller;

import com.moodwatch.social.dto.ApiResponse;
import com.moodwatch.social.dto.PagedResult;
import com.moodwatch.social.dto.ReviewRequest;
import com.moodwatch.social.dto.ReviewResponse;
import com.moodwatch.social.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<PagedResult<ReviewResponse>>> getByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getByMovie(movieId, page)));
    }
}
