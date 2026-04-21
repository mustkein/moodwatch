package com.moodwatch.movie.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moodwatch.movie.dto.MovieSummary;
import com.moodwatch.movie.dto.PagedResult;
import com.moodwatch.movie.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class TmdbClient {

    private static final String POSTER_BASE = "https://image.tmdb.org/t/p/w500";

    private final WebClient webClient;
    private final String apiKey;

    public TmdbClient(WebClient tmdbWebClient, @Value("${tmdb.api-key}") String apiKey) {
        this.webClient = tmdbWebClient;
        this.apiKey = apiKey;
    }

    public PagedResult<MovieSummary> search(String query, int page) {
        TmdbSearchResponse response = webClient.get()
                .uri(u -> u.path("/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .onErrorMap(e -> new ExternalApiException("TMDB request failed: " + e.getMessage()))
                .block();

        if (response == null) {
            throw new ExternalApiException("TMDB returned empty response");
        }

        List<MovieSummary> items = response.results().stream()
                .map(r -> new MovieSummary(
                        r.id(),
                        r.title(),
                        parseYear(r.releaseDate()),
                        r.posterPath() != null ? POSTER_BASE + r.posterPath() : null,
                        r.voteAverage(),
                        r.overview()
                ))
                .toList();

        return new PagedResult<>(items, response.page(), response.totalPages(), response.totalResults());
    }

    private Integer parseYear(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4) return null;
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    record TmdbSearchResponse(
            int page,
            List<TmdbMovie> results,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("total_results") long totalResults
    ) {}

    record TmdbMovie(
            Long id,
            String title,
            String overview,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("vote_average") Double voteAverage
    ) {}
}
