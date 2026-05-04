package com.moodwatch.movie.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moodwatch.movie.dto.CastMemberDto;
import com.moodwatch.movie.dto.MovieDetailDto;
import com.moodwatch.movie.dto.MovieSummary;
import com.moodwatch.movie.dto.PagedResult;
import com.moodwatch.movie.exception.ExternalApiException;
import com.moodwatch.movie.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class TmdbClient {

    private static final String POSTER_BASE = "https://image.tmdb.org/t/p/w500";
    private static final String BACKDROP_BASE = "https://image.tmdb.org/t/p/w1280";
    private static final String PROFILE_BASE = "https://image.tmdb.org/t/p/w185";

    private final WebClient webClient;
    private final String apiKey;

    public TmdbClient(WebClient tmdbWebClient, @Value("${tmdb.api-key}") String apiKey) {
        this.webClient = tmdbWebClient;
        this.apiKey = apiKey;
    }

    public PagedResult<MovieSummary> search(String query, int page) {
        int tmdbPage = page < 1 ? 1 : page;
        System.err.println("[TMDB] search called — query=\"" + query + "\" page=" + tmdbPage);
        TmdbSearchResponse response;
        try {
            response = webClient.get()
                    .uri(u -> u.path("/search/movie")
                            .queryParam("api_key", apiKey)
                            .queryParam("query", query)
                            .queryParam("page", tmdbPage)
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbSearchResponse.class)
                    .onErrorMap(e -> new ExternalApiException("TMDB request failed: " + e.getMessage()))
                    .block();
            System.err.println("[TMDB] response received — page=" + (response != null ? response.page() : "null")
                    + " totalResults=" + (response != null ? response.totalResults() : "null"));
        } catch (Exception e) {
            System.err.println("[TMDB] exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }

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

    public MovieDetailDto getMovieDetail(long tmdbId) {
        TmdbMovieDetail detail = webClient.get()
                .uri(u -> u.path("/movie/{id}")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "tr-TR")
                        .queryParam("append_to_response", "videos,credits")
                        .build(tmdbId))
                .retrieve()
                .bodyToMono(TmdbMovieDetail.class)
                .onErrorMap(e -> new ExternalApiException("TMDB request failed: " + e.getMessage()))
                .block();

        if (detail == null) {
            throw new NotFoundException("Movie not found: " + tmdbId);
        }

        List<String> genres = detail.genres() != null
                ? detail.genres().stream().map(TmdbGenre::name).toList()
                : List.of();

        String trailerKey = null;
        if (detail.videos() != null && detail.videos().results() != null) {
            trailerKey = detail.videos().results().stream()
                    .filter(v -> "YouTube".equals(v.site()) && "Trailer".equals(v.type()))
                    .findFirst()
                    .map(TmdbVideo::key)
                    .orElse(null);
        }

        String trailerUrl = trailerKey != null ? "https://www.youtube.com/watch?v=" + trailerKey : null;

        List<CastMemberDto> cast = List.of();
        if (detail.credits() != null && detail.credits().cast() != null) {
            cast = detail.credits().cast().stream()
                    .limit(8)
                    .map(c -> new CastMemberDto(
                            c.name(),
                            c.character(),
                            c.profilePath() != null ? PROFILE_BASE + c.profilePath() : null
                    ))
                    .toList();
        }

        return new MovieDetailDto(
                detail.id(),
                detail.title(),
                detail.overview(),
                detail.posterPath() != null ? POSTER_BASE + detail.posterPath() : null,
                detail.backdropPath() != null ? BACKDROP_BASE + detail.backdropPath() : null,
                detail.voteAverage() != null ? detail.voteAverage() : 0.0,
                detail.releaseDate(),
                detail.runtime() != null ? detail.runtime() : 0,
                genres,
                trailerKey,
                trailerUrl,
                cast
        );
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

    record TmdbMovieDetail(
            long id,
            String title,
            String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("backdrop_path") String backdropPath,
            @JsonProperty("vote_average") Double voteAverage,
            @JsonProperty("release_date") String releaseDate,
            Integer runtime,
            List<TmdbGenre> genres,
            TmdbVideoList videos,
            TmdbCredits credits
    ) {}

    record TmdbCredits(List<TmdbCastMember> cast) {}

    record TmdbCastMember(
            String name,
            String character,
            @JsonProperty("profile_path") String profilePath
    ) {}

    record TmdbGenre(long id, String name) {}

    record TmdbVideoList(List<TmdbVideo> results) {}

    record TmdbVideo(String key, String site, String type) {}
}
