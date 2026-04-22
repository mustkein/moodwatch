package com.moodwatch.social.service;

import com.moodwatch.social.dto.PagedResult;
import com.moodwatch.social.dto.ReviewRequest;
import com.moodwatch.social.dto.ReviewResponse;
import com.moodwatch.social.entity.Review;
import com.moodwatch.social.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public ReviewResponse create(UUID userId, ReviewRequest request) {
        Review review = new Review();
        review.setUserId(userId);
        review.setMovieId(request.movieId());
        review.setRating(request.rating());
        review.setText(request.text());
        review.setMoodTag(request.moodTag());
        return toResponse(reviewRepository.save(review));
    }

    public PagedResult<ReviewResponse> getByMovie(Long movieId, int page) {
        Page<Review> result = reviewRepository.findByMovieIdOrderByCreatedAtDesc(
                movieId, PageRequest.of(page - 1, 20));
        List<ReviewResponse> items = result.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PagedResult<>(items, result.getTotalElements(), result.getTotalPages(), page);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getMovieId(),
                review.getRating(),
                review.getText(),
                review.getMoodTag(),
                review.getCreatedAt()
        );
    }
}
