package com.moodwatch.recommendation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodwatch.recommendation.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class GroqClient {

    private final WebClient webClient;
    private final String model;

    public GroqClient(
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.base-url}") String baseUrl,
            @Value("${groq.model}") String model) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        System.err.println("[Groq] apiKey prefix: " + apiKey.substring(0, Math.min(10, apiKey.length()))
                + (apiKey.isEmpty() ? " (EMPTY - check GROQ_API_KEY env var)" : "..."));
    }

    public String generateContent(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 1000
        );

        System.err.println("[Groq] prompt:\n" + prompt);

        try {
            String rawBody = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                            .doOnNext(errBody -> System.err.println(
                                    "[Groq] error status " + response.statusCode() + " body:\n" + errBody))
                            .map(errBody -> new ExternalApiException("groq error " + response.statusCode())))
                    .bodyToMono(String.class)
                    .block();

            System.err.println("[Groq] response:\n" + rawBody);

            Map<?, ?> response = new ObjectMapper().readValue(rawBody, Map.class);
            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            System.err.println("[Groq] exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new ExternalApiException("groq unavailable");
        }
    }
}
