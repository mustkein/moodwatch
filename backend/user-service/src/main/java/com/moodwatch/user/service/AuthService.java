package com.moodwatch.user.service;

import com.moodwatch.user.dto.RegisterRequest;
import com.moodwatch.user.dto.UserResponse;
import com.moodwatch.user.entity.User;
import com.moodwatch.user.exception.ConflictException;
import com.moodwatch.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
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
}
