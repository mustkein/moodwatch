package com.moodwatch.social.repository;

import com.moodwatch.social.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId, Pageable pageable);
}
