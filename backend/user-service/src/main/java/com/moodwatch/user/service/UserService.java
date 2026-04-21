package com.moodwatch.user.service;

import com.moodwatch.user.dto.UserResponse;
import com.moodwatch.user.exception.AuthException;
import com.moodwatch.user.exception.NotFoundException;
import com.moodwatch.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final JwtService jwtService;

    public UserService(UserRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    public UserResponse getMe(String accessToken) {
        if (!jwtService.isTokenValid(accessToken)) {
            throw new AuthException("invalid token");
        }
        UUID userId = jwtService.extractUserId(accessToken);
        return userRepo.findById(userId)
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt()))
                .orElseThrow(() -> new NotFoundException("user not found"));
    }
}
