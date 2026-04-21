package com.moodwatch.user.controller;

import com.moodwatch.user.dto.ApiResponse;
import com.moodwatch.user.dto.LoginRequest;
import com.moodwatch.user.dto.LoginResponse;
import com.moodwatch.user.dto.RefreshRequest;
import com.moodwatch.user.dto.RefreshResponse;
import com.moodwatch.user.dto.RegisterRequest;
import com.moodwatch.user.dto.UserResponse;
import com.moodwatch.user.exception.AuthException;
import com.moodwatch.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(extractBearer(authHeader));
        return ResponseEntity.noContent().build();
    }

    static String extractBearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new AuthException("invalid token");
        }
        return header.substring(7);
    }
}
