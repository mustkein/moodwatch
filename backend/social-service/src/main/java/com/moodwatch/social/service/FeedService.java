package com.moodwatch.social.service;

import com.moodwatch.social.dto.PagedResult;
import com.moodwatch.social.entity.ActivityFeedItem;
import com.moodwatch.social.repository.ActivityFeedRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FeedService {

    private final ActivityFeedRepository feedRepository;

    public FeedService(ActivityFeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    public void recordReview(UUID userId, Long movieId, Integer rating, String text) {
        Map<String, Object> payload = Map.of(
                "rating", rating,
                "text", text != null ? text : ""
        );
        feedRepository.save(new ActivityFeedItem(userId, "REVIEW", movieId, payload));
    }

    public void recordWatched(UUID userId, Long movieId, Integer rating) {
        Map<String, Object> payload = rating != null
                ? Map.of("rating", rating)
                : Map.of();
        feedRepository.save(new ActivityFeedItem(userId, "WATCHED", movieId, payload));
    }

    public PagedResult<ActivityFeedItem> getFeed(List<UUID> followedUserIds, int page) {
        PageRequest pageable = PageRequest.of(page - 1, 20);
        List<ActivityFeedItem> items = feedRepository.findByUserIdInOrderByCreatedAtDesc(followedUserIds, pageable);
        return new PagedResult<>(items, items.size(), 1, page);
    }
}
