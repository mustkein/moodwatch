package com.moodwatch.desktop.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String baseUrl = "http://localhost:8080";

    private String authToken;
    private String currentUsername;
    private Long userId;

    private ApiClient() {}

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public String getAuthToken() { return authToken; }
    public String getUsername() { return currentUsername; }
    public Long getUserId() { return userId; }

    public record LoginResponse(String accessToken, String refreshToken, Long userId) {}
    public record MovieSummary(Long tmdbId, String title, Integer year, String posterUrl, Double rating, String overview) {}
    public record PagedResult<T>(java.util.List<T> items, int page, int totalPages, long totalItems) {}
    public record RecommendationItem(String title, String reason, Double rating, String year) {}
    public record RecommendationResponse(java.util.List<RecommendationItem> items) {}

    private record ApiResponse<T>(boolean success, T data, String error) {}

    public LoginResponse login(String username, String password) {
        try {
            String body = mapper.writeValueAsString(
                    new java.util.HashMap<>() {{
                        put("username", username);
                        put("password", password);
                    }}
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                throw new RuntimeException("Invalid credentials");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("Server error response body: " + response.body());
                throw new RuntimeException("Server error");
            }

            ApiResponse<LoginResponse> result = mapper.readValue(
                    response.body(),
                    new TypeReference<ApiResponse<LoginResponse>>() {}
            );
            this.authToken = result.data().accessToken();
            this.currentUsername = username;
            this.userId = result.data().userId();
            return result.data();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Server error", e);
        }
    }

    public java.util.List<MovieSummary> searchMovies(String query, int page) {
        System.out.println("[API] searchMovies called with query=" + query + " page=" + page);
        try {
            String encoded = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            String url = "http://localhost:8080/api/movies/search?q=" + encoded + "&page=" + page;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Search failed: " + response.statusCode());
            }

            ApiResponse<PagedResult<MovieSummary>> result = mapper.readValue(
                    response.body(),
                    new TypeReference<ApiResponse<PagedResult<MovieSummary>>>() {}
            );
            return result.data() != null ? result.data().items() : java.util.List.of();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Search error", e);
        }
    }

    public java.util.List<RecommendationItem> getRecommendations(String mood, double minRating, Integer maxRuntime) {
        try {
            java.util.Map<String, Object> bodyMap = new java.util.HashMap<>();
            bodyMap.put("mood", mood);
            bodyMap.put("minRating", minRating);
            if (maxRuntime != null) bodyMap.put("maxRuntime", maxRuntime);

            String body = mapper.writeValueAsString(bodyMap);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8084/api/recommendations/mood"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken);
            if (userId != null) builder.header("X-User-Id", String.valueOf(userId));

            HttpRequest request = builder
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Öneri alınamadı: " + response.statusCode());
            }

            ApiResponse<RecommendationResponse> result = mapper.readValue(
                    response.body(),
                    new TypeReference<ApiResponse<RecommendationResponse>>() {}
            );
            if (result.data() == null || result.data().items() == null) return java.util.List.of();
            return result.data().items();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Recommendation error", e);
        }
    }
}
