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

    private ApiClient() {}

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public String getAuthToken() {
        return authToken;
    }

    public record LoginResponse(String accessToken, String refreshToken) {}

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
            return result.data();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Server error", e);
        }
    }
}
