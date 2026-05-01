package com.moodwatch.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodwatch.recommendation.client.GroqClient;
import com.moodwatch.recommendation.dto.RecommendationRequest;
import com.moodwatch.recommendation.dto.RecommendationResponse;
import com.moodwatch.recommendation.exception.ExternalApiException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RecommendationService {

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    public RecommendationService(GroqClient groqClient) {
        this.groqClient = groqClient;
        this.objectMapper = new ObjectMapper();
    }

    public RecommendationResponse recommend(UUID userId, RecommendationRequest req) {
        String json = groqClient.generateContent(buildPrompt(req));
        try {
            return objectMapper.readValue(json, RecommendationResponse.class);
        } catch (Exception e) {
            throw new ExternalApiException("could not parse recommendation response");
        }
    }

    private String buildPrompt(RecommendationRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a film recommendation engine. ");
        sb.append("Based on the user's mood and preferences, recommend up to 5 films. ");
        sb.append("Return ONLY valid JSON in this exact shape, no extra text: ");
        sb.append("{\"items\":[{\"title\":\"...\",\"reason\":\"...\",\"rating\":7.5,\"year\":\"...\"}]}\n\n");
        sb.append("Mood: ").append(req.mood()).append("\n");
        sb.append("Minimum rating: ").append(req.minRating() != null ? req.minRating() : 0.0).append("\n");
        if (req.maxRuntime() != null) {
            sb.append("Maximum runtime: ").append(req.maxRuntime()).append(" minutes\n");
        }
        if (req.genres() != null && !req.genres().isEmpty()) {
            sb.append("Genres: ").append(String.join(", ", req.genres())).append("\n");
        }
        if (req.era() != null && !req.era().isBlank()) {
            sb.append("Era: ").append(req.era()).append("\n");
        }
        if (!req.includeWatched()) {
            sb.append("Prefer films the user has not already seen.\n");
        }
        return sb.toString();
    }
}
