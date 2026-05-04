package com.moodwatch.movie.dto;

import java.util.List;

public record MovieDetailDto(
        long tmdbId,
        String title,
        String overview,
        String posterUrl,
        String backdropUrl,
        double rating,
        String releaseDate,
        int runtime,
        List<String> genres,
        String trailerKey,
        String trailerUrl,
        List<CastMemberDto> cast
) {}
