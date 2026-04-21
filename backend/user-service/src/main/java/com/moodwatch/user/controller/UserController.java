package com.moodwatch.user.controller;

import com.moodwatch.user.dto.ApiResponse;
import com.moodwatch.user.dto.UserResponse;
import com.moodwatch.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@RequestHeader("Authorization") String authHeader) {
        UserResponse response = userService.getMe(AuthController.extractBearer(authHeader));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
