package com.moodwatch.movie.controller;

import com.moodwatch.movie.dto.ApiResponse;
import com.moodwatch.movie.dto.MovieSummary;
import com.moodwatch.movie.dto.PagedResult;
import com.moodwatch.movie.service.MovieService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@Validated
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.ok("pong"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<MovieSummary>>> search(
            @RequestParam @NotBlank String q,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.search(q, page)));
    }
}
