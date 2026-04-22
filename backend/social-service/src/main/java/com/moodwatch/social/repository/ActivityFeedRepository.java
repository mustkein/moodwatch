package com.moodwatch.social.repository;

import com.moodwatch.social.entity.ActivityFeedItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityFeedRepository extends MongoRepository<ActivityFeedItem, String> {
    List<ActivityFeedItem> findByUserIdInOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);
}
