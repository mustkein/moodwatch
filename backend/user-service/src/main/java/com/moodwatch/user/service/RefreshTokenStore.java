package com.moodwatch.user.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redis;

    public RefreshTokenStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void save(UUID userId, String refreshToken, long ttlDays) {
        redis.opsForValue().set(KEY_PREFIX + userId, refreshToken, Duration.ofDays(ttlDays));
    }

    public Optional<String> find(UUID userId) {
        return Optional.ofNullable(redis.opsForValue().get(KEY_PREFIX + userId));
    }

    public void delete(UUID userId) {
        redis.delete(KEY_PREFIX + userId);
    }
}
