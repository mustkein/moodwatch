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
    private final String baseUrl = "http://localhost:8081";

    private String authToken;
    private String currentUsername;

    private ApiClient() {}

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public String getAuthToken() { return authToken; }
    public String getUsername() { return currentUsername; }

    public record LoginResponse(String accessToken, String refreshToken) {}
    public record MovieSummary(Long tmdbId, String title, Integer year, String posterUrl, Double rating, String overview) {}
    public record PagedResult<T>(java.util.List<T> items, int page, int totalPages, long totalItems) {}

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
            String url = "http://localhost:8083/api/movies/search?q=" + encoded + "&page=" + page;

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
}
