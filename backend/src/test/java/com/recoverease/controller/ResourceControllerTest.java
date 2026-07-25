package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.TestSecurityUtils;
import com.recoverease.entity.ResourceItem;
import com.recoverease.entity.User;
import com.recoverease.repository.UserRepository;
import com.recoverease.service.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private ResourceService resourceService;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("resource_ctrl_test@example.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("Resource User");
                    u.setEmail("resource_ctrl_test@example.com");
                    u.setPasswordHash("$2a$10$dummy");
                    u.setRole("INDIVIDUAL");
                    return userRepository.save(u);
                });
    }

    // ─── GET /api/resources (requires auth per SecurityConfig) ───────────────

    @Test
    void getAllResources_withoutAuth_returns401or403() throws Exception {
        int status = mockMvc.perform(get("/api/resources")).andReturn().getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    @Test
    void getAllResources_noParams_returns200WithAllResources() throws Exception {
        when(resourceService.getAllResources())
                .thenReturn(List.of(buildResource("Understanding Addiction", "EDUCATION"),
                        buildResource("SAMHSA Hotline", "HOTLINE")));

        mockMvc.perform(get("/api/resources").with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Understanding Addiction"))
                .andExpect(jsonPath("$[1].title").value("SAMHSA Hotline"));
    }

    @Test
    void getAllResources_withCategory_returns200WithFilteredResources() throws Exception {
        when(resourceService.getByCategory("EDUCATION"))
                .thenReturn(List.of(buildResource("Addiction Facts", "EDUCATION")));

        mockMvc.perform(get("/api/resources")
                        .param("category", "EDUCATION")
                        .with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Addiction Facts"));
    }

    @Test
    void getAllResources_withSearchQuery_returns200WithSearchResults() throws Exception {
        when(resourceService.search("recovery"))
                .thenReturn(List.of(buildResource("Recovery Steps", "EDUCATION")));

        mockMvc.perform(get("/api/resources")
                        .param("search", "recovery")
                        .with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Recovery Steps"));
    }

    @Test
    void getAllResources_searchTakesPriorityOverCategory() throws Exception {
        when(resourceService.search("addiction"))
                .thenReturn(List.of(buildResource("Search Result", "EDUCATION")));

        mockMvc.perform(get("/api/resources")
                        .param("search", "addiction")
                        .param("category", "EDUCATION")
                        .with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Search Result"));
    }

    @Test
    void getAllResources_emptyResult_returns200WithEmptyList() throws Exception {
        when(resourceService.getAllResources()).thenReturn(List.of());

        mockMvc.perform(get("/api/resources").with(TestSecurityUtils.asUser(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── POST /api/resources/explain ──────────────────────────────────────────

    @Test
    void explainTopic_validQuestion_returns200WithAnswer() throws Exception {
        when(resourceService.explainTopic("What is addiction?"))
                .thenReturn(Map.of(
                        "answer", "Addiction is a chronic condition affecting the brain.",
                        "source", "SAMHSA"
                ));

        String body = objectMapper.writeValueAsString(Map.of("question", "What is addiction?"));

        mockMvc.perform(post("/api/resources/explain")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Addiction is a chronic condition affecting the brain."))
                .andExpect(jsonPath("$.source").value("SAMHSA"));
    }

    @Test
    void explainTopic_emptyQuestion_returns400WithError() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("question", ""));

        mockMvc.perform(post("/api/resources/explain")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Question is required"));
    }

    @Test
    void explainTopic_blankQuestion_returns400WithError() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("question", "   "));

        mockMvc.perform(post("/api/resources/explain")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Question is required"));
    }

    @Test
    void explainTopic_missingQuestionKey_returns400WithError() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("topic", "addiction"));

        mockMvc.perform(post("/api/resources/explain")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Question is required"));
    }

    private ResourceItem buildResource(String title, String category) {
        ResourceItem item = new ResourceItem();
        item.setTitle(title);
        item.setCategory(category);
        return item;
    }
}
