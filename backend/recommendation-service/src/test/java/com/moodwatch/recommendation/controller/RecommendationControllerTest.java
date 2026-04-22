package com.moodwatch.recommendation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodwatch.recommendation.dto.RecommendationItem;
import com.moodwatch.recommendation.dto.RecommendationRequest;
import com.moodwatch.recommendation.dto.RecommendationResponse;
import com.moodwatch.recommendation.exception.GlobalExceptionHandler;
import com.moodwatch.recommendation.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecommendationController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    void postMoodWithValidRequestReturns200() throws Exception {
        UUID userId = UUID.randomUUID();
        RecommendationRequest request = new RecommendationRequest(
                "happy", 7.0, null, List.of("comedy"), null, false);

        RecommendationResponse response = new RecommendationResponse(List.of(
                new RecommendationItem("Some Like It Hot", "Classic comedy matching your mood", 8.2, "1959"),
                new RecommendationItem("The Grand Budapest Hotel", "Whimsical and uplifting", 8.1, "2014"),
                new RecommendationItem("Paddington 2", "Joyful and warm", 7.8, "2017")
        ));

        when(recommendationService.recommend(eq(userId), any(RecommendationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/recommendations/mood")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(3))
                .andExpect(jsonPath("$.data.items[0].title").value("Some Like It Hot"));
    }

    @Test
    void postMoodWithBlankMoodReturns400() throws Exception {
        RecommendationRequest request = new RecommendationRequest(
                "", null, null, null, null, false);

        mockMvc.perform(post("/api/recommendations/mood")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
