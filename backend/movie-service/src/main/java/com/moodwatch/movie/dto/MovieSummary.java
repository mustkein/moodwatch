package com.moodwatch.movie.dto;

public record MovieSummary(
        Long tmdbId,
        String title,
        Integer year,
        String posterUrl,
        Double rating,
        String overview
) {}
