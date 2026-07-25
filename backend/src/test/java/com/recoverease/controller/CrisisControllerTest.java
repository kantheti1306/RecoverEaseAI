package com.recoverease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.TestSecurityUtils;
import com.recoverease.dto.CrisisRequest;
import com.recoverease.dto.CrisisResponseDto;
import com.recoverease.entity.User;
import com.recoverease.repository.UserRepository;
import com.recoverease.service.CrisisService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrisisControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private CrisisService crisisService;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("crisis_ctrl_test@example.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("Alex");
                    u.setEmail("crisis_ctrl_test@example.com");
                    u.setPasswordHash("$2a$10$dummy");
                    u.setRole("INDIVIDUAL");
                    return userRepository.save(u);
                });
    }

    @Test
    void respond_withoutAuth_returns401or403() throws Exception {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("I need help");

        int status = mockMvc.perform(post("/api/crisis/respond")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    @Test
    void respond_validRequest_returns200WithDto() throws Exception {
        CrisisResponseDto dto = new CrisisResponseDto();
        dto.setRiskLevel("MEDIUM");
        dto.setMessage("You are not alone. Take one breath at a time.");
        dto.setSteps(List.of("Breathe slowly", "Call your contact"));
        dto.setScript("I need help right now.");
        dto.setEscalate(false);
        dto.setContactName("Mom");
        dto.setContactPhone("555-1234");
        dto.setTtsText("You are not alone. Take one breath at a time.");

        when(crisisService.respond(any(CrisisRequest.class), any(User.class))).thenReturn(dto);

        CrisisRequest req = new CrisisRequest();
        req.setInputText("I am feeling overwhelmed");

        mockMvc.perform(post("/api/crisis/respond")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("MEDIUM"))
                .andExpect(jsonPath("$.escalate").value(false))
                .andExpect(jsonPath("$.contactName").value("Mom"))
                .andExpect(jsonPath("$.ttsText").value("You are not alone. Take one breath at a time."));
    }

    @Test
    void respond_highRiskEscalation_returns200WithEscalateTrue() throws Exception {
        CrisisResponseDto dto = new CrisisResponseDto();
        dto.setRiskLevel("HIGH");
        dto.setMessage("Call 911 immediately.");
        dto.setSteps(List.of("Call 911 now"));
        dto.setScript("Emergency.");
        dto.setEscalate(true);

        when(crisisService.respond(any(CrisisRequest.class), any(User.class))).thenReturn(dto);

        CrisisRequest req = new CrisisRequest();
        req.setInputText("I overdosed");

        mockMvc.perform(post("/api/crisis/respond")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.escalate").value(true));
    }

    @Test
    void respond_missingInputText_returns400() throws Exception {
        String body = "{\"mode\": \"text\"}";

        mockMvc.perform(post("/api/crisis/respond")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void respond_blankInputText_returns400() throws Exception {
        String body = "{\"inputText\": \"   \"}";

        mockMvc.perform(post("/api/crisis/respond")
                        .with(TestSecurityUtils.asUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
