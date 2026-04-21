package com.moodwatch.movie.service;

import com.moodwatch.movie.dto.MovieSummary;
import com.moodwatch.movie.dto.PagedResult;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private final TmdbClient tmdbClient;

    public MovieService(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    public PagedResult<MovieSummary> search(String query, int page) {
        return tmdbClient.search(query, page);
    }
}
