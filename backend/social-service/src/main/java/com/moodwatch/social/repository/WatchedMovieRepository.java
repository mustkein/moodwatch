package com.moodwatch.social.repository;

import com.moodwatch.social.entity.WatchedMovie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WatchedMovieRepository extends JpaRepository<WatchedMovie, UUID> {}
