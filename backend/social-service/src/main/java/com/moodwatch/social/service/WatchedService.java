package com.moodwatch.social.service;

import com.moodwatch.social.dto.WatchedRequest;
import com.moodwatch.social.entity.WatchedMovie;
import com.moodwatch.social.repository.WatchedMovieRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WatchedService {

    private final WatchedMovieRepository watchedMovieRepository;
    private final FeedService feedService;

    public WatchedService(WatchedMovieRepository watchedMovieRepository, FeedService feedService) {
        this.watchedMovieRepository = watchedMovieRepository;
        this.feedService = feedService;
    }

    public void markWatched(UUID userId, WatchedRequest request) {
        WatchedMovie watched = new WatchedMovie();
        watched.setUserId(userId);
        watched.setMovieId(request.movieId());
        watched.setRating(request.rating());
        watchedMovieRepository.save(watched);
        feedService.recordWatched(userId, request.movieId(), request.rating());
    }
}
