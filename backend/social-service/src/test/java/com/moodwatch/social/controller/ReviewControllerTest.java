package com.moodwatch.social.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodwatch.social.dto.PagedResult;
import com.moodwatch.social.dto.ReviewRequest;
import com.moodwatch.social.dto.ReviewResponse;
import com.moodwatch.social.exception.GlobalExceptionHandler;
import com.moodwatch.social.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReviewController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @Test
    void postReviewWithValidDataReturns201() throws Exception {
        UUID userId = UUID.randomUUID();
        ReviewRequest request = new ReviewRequest(550L, 8, "Great film", "HAPPY");
        ReviewResponse response = new ReviewResponse(UUID.randomUUID(), userId, 550L, 8, "Great film", "HAPPY", Instant.now());

        when(reviewService.create(eq(userId), any(ReviewRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.movieId").value(550))
                .andExpect(jsonPath("$.data.rating").value(8));
    }

    @Test
    void postReviewWithInvalidRatingReturns400() throws Exception {
        ReviewRequest request = new ReviewRequest(550L, 0, "text", null);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void getReviewsByMovieReturns200() throws Exception {
        PagedResult<ReviewResponse> paged = new PagedResult<>(List.of(), 0L, 0, 1);
        when(reviewService.getByMovie(550L, 1)).thenReturn(paged);

        mockMvc.perform(get("/api/reviews/movie/550").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }
}
