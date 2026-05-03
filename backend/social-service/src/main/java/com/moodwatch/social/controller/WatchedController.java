package com.moodwatch.social.controller;

import com.moodwatch.social.dto.ApiResponse;
import com.moodwatch.social.dto.WatchedMovieResponse;
import com.moodwatch.social.dto.WatchedRequest;
import com.moodwatch.social.service.WatchedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/watched")
public class WatchedController {

    private final WatchedService watchedService;

    public WatchedController(WatchedService watchedService) {
        this.watchedService = watchedService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WatchedMovieResponse>>> getWatchedMovies(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("UNAUTHORIZED", "X-User-Id header required"));
        }
        return ResponseEntity.ok(ApiResponse.ok(watchedService.getWatchedMovies(userId)));
    }

    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<ApiResponse<Void>> removeWatched(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable Long tmdbId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("UNAUTHORIZED", "X-User-Id header required"));
        }
        watchedService.removeWatched(userId, tmdbId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> markWatched(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody WatchedRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("UNAUTHORIZED", "X-User-Id header required"));
        }
        watchedService.markWatched(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }
}
