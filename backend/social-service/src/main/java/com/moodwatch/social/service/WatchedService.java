package com.moodwatch.social.service;

import com.moodwatch.social.dto.WatchedMovieResponse;
import com.moodwatch.social.dto.WatchedRequest;
import com.moodwatch.social.entity.WatchedMovie;
import com.moodwatch.social.repository.WatchedMovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WatchedService {

    private final WatchedMovieRepository watchedMovieRepository;
    private final FeedService feedService;

    public WatchedService(WatchedMovieRepository watchedMovieRepository, FeedService feedService) {
        this.watchedMovieRepository = watchedMovieRepository;
        this.feedService = feedService;
    }

    public List<WatchedMovieResponse> getWatchedMovies(UUID userId) {
        return watchedMovieRepository.findByUserId(userId).stream()
                .map(w -> new WatchedMovieResponse(
                        w.getTmdbId(),
                        w.getTitle(),
                        w.getWatchedAt().toString()))
                .toList();
    }

    public void removeWatched(UUID userId, Long tmdbId) {
        watchedMovieRepository.deleteByUserIdAndTmdbId(userId, tmdbId);
    }

    public void markWatched(UUID userId, WatchedRequest request) {
        WatchedMovie watched = new WatchedMovie();
        watched.setUserId(userId);
        watched.setTmdbId(request.tmdbId());
        watched.setTitle(request.title());
        watched.setRating(request.rating());
        watchedMovieRepository.save(watched);
        Integer ratingInt = request.rating() != null ? request.rating().intValue() : null;
        feedService.recordWatched(userId, request.tmdbId(), ratingInt);
    }
}
