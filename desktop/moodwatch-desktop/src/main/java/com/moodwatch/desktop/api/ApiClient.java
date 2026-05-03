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
    private String userId;

    private ApiClient() {}

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public String getAuthToken() { return authToken; }
    public String getUsername() { return currentUsername; }
    public String getUserId() { return userId; }

    public record LoginResponse(String accessToken, String refreshToken, String id, Long userId) {}
    public record RegisterResponse(Long id, String username, String email) {}
    public record MovieSummary(Long tmdbId, String title, Integer year, String posterUrl, Double rating, String overview) {}
    public record WatchedMovie(Long tmdbId, String title, String watchedAt) {}
    public record PagedResult<T>(java.util.List<T> items, int page, int totalPages, long totalItems) {}
    public record RecommendationItem(String title, String reason, Double rating, String year) {}
    public record RecommendationResponse(java.util.List<RecommendationItem> items) {}

    private record ApiResponse<T>(boolean success, T data, String error) {}

    private String extractMessage(String body) {
        try {
            java.util.Map<?, ?> map = mapper.readValue(body, java.util.Map.class);
            Object msg = map.get("message");
            if (msg != null) return msg.toString();
            Object err = map.get("error");
            if (err != null) return err.toString();
        } catch (Exception ignored) {}
        return "Sunucu hatası";
    }

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
                throw new RuntimeException("Kullanıcı adı veya şifre yanlış");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(extractMessage(response.body()));
            }

            ApiResponse<LoginResponse> result = mapper.readValue(
                    response.body(),
                    new TypeReference<ApiResponse<LoginResponse>>() {}
            );
            System.out.println("[Login] raw response: " + response.body());
            System.out.println("[Login] accessToken=" + result.data().accessToken()
                    + " id=" + result.data().id()
                    + " userId=" + result.data().userId());
            this.authToken = result.data().accessToken();
            this.currentUsername = username;
            try {
                String[] parts = this.authToken.split("\\.");
                byte[] decoded = java.util.Base64.getUrlDecoder().decode(parts[1]);
                java.util.Map<?, ?> payload = mapper.readValue(decoded, java.util.Map.class);
                this.userId = payload.get("sub") != null ? payload.get("sub").toString() : null;
            } catch (Exception ex) {
                this.userId = result.data().id() != null
                        ? result.data().id()
                        : (result.data().userId() != null ? result.data().userId().toString() : null);
            }
            System.out.println("[Login] stored userId=" + this.userId);
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

    public LoginResponse register(String username, String email, String password) {
        try {
            java.util.Map<String, Object> bodyMap = new java.util.HashMap<>();
            bodyMap.put("username", username);
            bodyMap.put("email", email);
            bodyMap.put("password", password);
            String body = mapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201 && (response.statusCode() < 200 || response.statusCode() >= 300)) {
                throw new RuntimeException(extractMessage(response.body()));
            }

            return login(username, password);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Register error", e);
        }
    }

    public java.util.List<WatchedMovie> getWatchedMovies() {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/watched"))
                    .header("Authorization", "Bearer " + authToken);
            if (userId != null) builder.header("X-User-Id", userId);

            HttpRequest request = builder.GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Liste alınamadı: " + response.statusCode());
            }

            ApiResponse<java.util.List<WatchedMovie>> result = mapper.readValue(
                    response.body(),
                    new TypeReference<ApiResponse<java.util.List<WatchedMovie>>>() {}
            );
            return result.data() != null ? result.data() : java.util.List.of();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Watched movies error", e);
        }
    }

    public void markWatched(long tmdbId, String title) {
        try {
            java.util.Map<String, Object> bodyMap = new java.util.HashMap<>();
            bodyMap.put("tmdbId", tmdbId);
            bodyMap.put("title", title);
            String body = mapper.writeValueAsString(bodyMap);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/watched"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken);
            if (userId != null) builder.header("X-User-Id", userId);

            HttpRequest request = builder
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            throw new RuntimeException("markWatched error", e);
        }
    }

    public void removeWatched(long tmdbId) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/watched/" + tmdbId))
                    .header("Authorization", "Bearer " + authToken);
            if (userId != null) builder.header("X-User-Id", userId);
            HttpRequest request = builder.DELETE().build();
            http.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            throw new RuntimeException("removeWatched error", e);
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
