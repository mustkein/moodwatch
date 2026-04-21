package com.moodwatch.user.service;

import com.moodwatch.user.dto.LoginRequest;
import com.moodwatch.user.dto.LoginResponse;
import com.moodwatch.user.dto.RefreshRequest;
import com.moodwatch.user.dto.RefreshResponse;
import com.moodwatch.user.dto.RegisterRequest;
import com.moodwatch.user.dto.UserResponse;
import com.moodwatch.user.entity.User;
import com.moodwatch.user.exception.AuthException;
import com.moodwatch.user.exception.ConflictException;
import com.moodwatch.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder,
                       JwtService jwtService, RefreshTokenStore refreshTokenStore) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    public UserResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new ConflictException("username already exists");
        }
        if (userRepo.existsByEmail(req.email())) {
            throw new ConflictException("email already exists");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        User saved = userRepo.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getCreatedAt());
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new AuthException("invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AuthException("invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        refreshTokenStore.save(user.getId(), refreshToken, jwtService.getRefreshTtlDays());

        return new LoginResponse(accessToken, refreshToken, "Bearer", jwtService.getAccessTokenExpirySeconds());
    }

    public void logout(String accessToken) {
        if (!jwtService.isTokenValid(accessToken)) {
            throw new AuthException("invalid token");
        }
        UUID userId = jwtService.extractUserId(accessToken);
        refreshTokenStore.delete(userId);
    }

    public RefreshResponse refresh(RefreshRequest req) {
        if (!jwtService.isTokenValid(req.refreshToken())) {
            throw new AuthException("invalid refresh token");
        }
        UUID userId = jwtService.extractUserId(req.refreshToken());
        String stored = refreshTokenStore.find(userId)
                .orElseThrow(() -> new AuthException("invalid refresh token"));
        if (!stored.equals(req.refreshToken())) {
            throw new AuthException("invalid refresh token");
        }
        String username = userRepo.findById(userId)
                .orElseThrow(() -> new AuthException("invalid refresh token"))
                .getUsername();
        String newAccessToken = jwtService.generateAccessToken(userId, username);
        return new RefreshResponse(newAccessToken, "Bearer", jwtService.getAccessTokenExpirySeconds());
    }
}
