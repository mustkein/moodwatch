package com.moodwatch.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodwatch.user.dto.RefreshRequest;
import com.moodwatch.user.dto.RefreshResponse;
import com.moodwatch.user.exception.AuthException;
import com.moodwatch.user.exception.GlobalExceptionHandler;
import com.moodwatch.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class AuthControllerRefreshTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void refreshWithValidTokenReturnsNewAccessToken() throws Exception {
        RefreshRequest request = new RefreshRequest("valid.refresh.token");
        RefreshResponse response = new RefreshResponse("new.access.token", "Bearer", 900);

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void refreshWithInvalidTokenReturns401() throws Exception {
        RefreshRequest request = new RefreshRequest("invalid.token.value");

        when(authService.refresh(any(RefreshRequest.class)))
                .thenThrow(new AuthException("invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void refreshWithExpiredTokenReturns401() throws Exception {
        RefreshRequest request = new RefreshRequest("expired.refresh.token");

        when(authService.refresh(any(RefreshRequest.class)))
                .thenThrow(new AuthException("refresh token expired"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void refreshWithMismatchedTokenReturns401() throws Exception {
        RefreshRequest request = new RefreshRequest("revoked.refresh.token");

        when(authService.refresh(any(RefreshRequest.class)))
                .thenThrow(new AuthException("invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void refreshWithBlankTokenReturns400() throws Exception {
        RefreshRequest request = new RefreshRequest("");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
