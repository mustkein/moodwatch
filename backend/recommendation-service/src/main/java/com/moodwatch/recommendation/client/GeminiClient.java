package com.moodwatch.recommendation.client;

import com.moodwatch.recommendation.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public String generateContent(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        System.err.println("[Gemini] prompt:\n" + prompt);

        try {
            String rawBody = webClient.post()
                    .uri("/models/{model}:generateContent?key={key}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.err.println("[Gemini] response:\n" + rawBody);

            Map<?, ?> response = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(rawBody, Map.class);

            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts = (List<?>) content.get("parts");
            return (String) ((Map<?, ?>) parts.get(0)).get("text");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExternalApiException("gemini unavailable");
        }
    }
}
