package com.moodwatch.social.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watched_movies")
public class WatchedMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Long movieId;

    private Integer rating;

    @Column(nullable = false, updatable = false)
    private Instant watchedAt;

    @PrePersist
    void onCreate() {
        this.watchedAt = Instant.now();
    }

    public UUID getId() { return id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Instant getWatchedAt() { return watchedAt; }
}
