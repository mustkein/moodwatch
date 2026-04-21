package com.moodwatch.user.controller;

import com.moodwatch.user.dto.UserResponse;
import com.moodwatch.user.exception.AuthException;
import com.moodwatch.user.exception.GlobalExceptionHandler;
import com.moodwatch.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getMeReturnsCurrentUser() throws Exception {
        UserResponse response = new UserResponse(UUID.randomUUID(), "validuser", "valid@example.com", Instant.now());

        when(userService.getMe(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer valid.access.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("validuser"));
    }

    @Test
    void getMeWithInvalidTokenReturns401() throws Exception {
        when(userService.getMe(anyString()))
                .thenThrow(new AuthException("invalid token"));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid.access.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
