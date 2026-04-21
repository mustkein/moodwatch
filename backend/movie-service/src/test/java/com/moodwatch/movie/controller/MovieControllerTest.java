package com.moodwatch.movie.controller;

import com.moodwatch.movie.dto.MovieSummary;
import com.moodwatch.movie.dto.PagedResult;
import com.moodwatch.movie.exception.GlobalExceptionHandler;
import com.moodwatch.movie.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
@Import(GlobalExceptionHandler.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Test
    void searchReturnsMovies() throws Exception {
        List<MovieSummary> items = List.of(
                new MovieSummary(27205L, "Inception", 2010, "/poster1.jpg", 8.4, "A thief who steals secrets"),
                new MovieSummary(157336L, "Interstellar", 2014, "/poster2.jpg", 8.6, "A team of explorers")
        );
        PagedResult<MovieSummary> result = new PagedResult<>(items, 1, 1, 2L);

        when(movieService.search("inception", 1)).thenReturn(result);

        mockMvc.perform(get("/api/movies/search")
                        .param("q", "inception")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].title").exists())
                .andExpect(jsonPath("$.data.page").value(1));
    }

    @Test
    void searchWithBlankQueryReturns400() throws Exception {
        mockMvc.perform(get("/api/movies/search")
                        .param("q", "")
                        .param("page", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
