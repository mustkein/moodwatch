package com.moodwatch.social.controller;

import com.moodwatch.social.dto.ApiResponse;
import com.moodwatch.social.dto.WatchedRequest;
import com.moodwatch.social.service.WatchedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/watched")
public class WatchedController {

    private final WatchedService watchedService;

    public WatchedController(WatchedService watchedService) {
        this.watchedService = watchedService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> markWatched(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody WatchedRequest request) {
        watchedService.markWatched(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }
}
