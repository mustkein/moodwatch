package com.moodwatch.social.controller;

import com.moodwatch.social.dto.ApiResponse;
import com.moodwatch.social.dto.PagedResult;
import com.moodwatch.social.entity.ActivityFeedItem;
import com.moodwatch.social.service.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<ActivityFeedItem>>> getFeed(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "") String following,
            @RequestParam(defaultValue = "1") int page) {
        List<UUID> followedUserIds = following.isBlank()
                ? List.of()
                : Arrays.stream(following.split(","))
                        .map(String::trim)
                        .map(UUID::fromString)
                        .toList();
        return ResponseEntity.ok(ApiResponse.ok(feedService.getFeed(followedUserIds, page)));
    }
}
