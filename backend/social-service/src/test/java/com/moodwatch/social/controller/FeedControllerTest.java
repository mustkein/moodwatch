package com.moodwatch.social.controller;

import com.moodwatch.social.dto.PagedResult;
import com.moodwatch.social.entity.ActivityFeedItem;
import com.moodwatch.social.exception.GlobalExceptionHandler;
import com.moodwatch.social.service.FeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FeedController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedService feedService;

    @Test
    void getFeedReturns200WithItems() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        ActivityFeedItem item = new ActivityFeedItem(followedId, "REVIEW", 550L, java.util.Map.of("rating", 8));
        PagedResult<ActivityFeedItem> paged = new PagedResult<>(List.of(item), 1L, 1, 1);

        when(feedService.getFeed(any(), eq(1))).thenReturn(paged);

        mockMvc.perform(get("/api/feed")
                        .header("X-User-Id", userId.toString())
                        .param("following", followedId.toString())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].type").value("REVIEW"));
    }
}
