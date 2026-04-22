package com.moodwatch.social.service;

import com.moodwatch.social.dto.WatchedRequest;
import com.moodwatch.social.entity.WatchedMovie;
import com.moodwatch.social.repository.WatchedMovieRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WatchedService {

    private final WatchedMovieRepository watchedMovieRepository;

    public WatchedService(WatchedMovieRepository watchedMovieRepository) {
        this.watchedMovieRepository = watchedMovieRepository;
    }

    public void markWatched(UUID userId, WatchedRequest request) {
        WatchedMovie watched = new WatchedMovie();
        watched.setUserId(userId);
        watched.setMovieId(request.movieId());
        watched.setRating(request.rating());
        watchedMovieRepository.save(watched);
    }
}
