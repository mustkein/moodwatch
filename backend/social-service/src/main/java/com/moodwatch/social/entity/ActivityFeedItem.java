package com.moodwatch.social.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document("activity_feed")
public class ActivityFeedItem {

    @Id
    private String id;

    private UUID userId;
    private String type;
    private Long movieId;
    private Map<String, Object> payload;
    private Instant createdAt;

    public ActivityFeedItem() {}

    public ActivityFeedItem(UUID userId, String type, Long movieId, Map<String, Object> payload) {
        this.userId = userId;
        this.type = type;
        this.movieId = movieId;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
