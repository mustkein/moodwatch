package com.moodwatch.user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GlobalExceptionHandlerTest.FakeController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class FakeController {

        @GetMapping("/test/not-found")
        String notFound() { throw new NotFoundException("resource not found"); }

        @GetMapping("/test/validation")
        String validation() { throw new ValidationException("invalid input"); }

        @GetMapping("/test/auth")
        String auth() { throw new AuthException("unauthorized"); }

        @GetMapping("/test/external-api")
        String externalApi() { throw new ExternalApiException("tmdb unavailable"); }
    }

    @Test
    void notFoundReturns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("resource not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validationReturns400() throws Exception {
        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("invalid input"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void authReturns401() throws Exception {
        mockMvc.perform(get("/test/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("unauthorized"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void externalApiReturns502() throws Exception {
        mockMvc.perform(get("/test/external-api"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("EXTERNAL_API_ERROR"))
                .andExpect(jsonPath("$.message").value("tmdb unavailable"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
