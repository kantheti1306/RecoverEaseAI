package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.CrisisRequest;
import com.recoverease.dto.CrisisResponseDto;
import com.recoverease.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrisisServiceTest {

    @Mock
    private GeminiAiService geminiAiService;

    @InjectMocks
    private CrisisService crisisService;

    private User testUser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        try {
            var field = CrisisService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(crisisService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Alex");
        testUser.setEmail("alex@example.com");
        testUser.setRole("INDIVIDUAL");
        testUser.setPrimaryContactName("Mom");
        testUser.setPrimaryContactPhone("555-1234");
        testUser.setCalmingStrategies("deep breathing");
        testUser.setTriggers("stress");
        testUser.setPersonalReminder("You are strong.");
    }

    @Test
    void respond_normalInput_returnsCrisisResponse() {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("I am feeling stressed today");

        String aiJson = """
                {
                  "riskLevel": "LOW",
                  "message": "Take a breath. You can do this.",
                  "steps": ["Breathe slowly", "Call a friend"],
                  "script": "I need support.",
                  "escalate": false
                }
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CrisisResponseDto result = crisisService.respond(req, testUser);

        assertThat(result).isNotNull();
        assertThat(result.getRiskLevel()).isEqualTo("LOW");
        assertThat(result.isEscalate()).isFalse();
        assertThat(result.getContactName()).isEqualTo("Mom");
        assertThat(result.getContactPhone()).isEqualTo("555-1234");
        assertThat(result.getTtsText()).isEqualTo(result.getMessage());
    }

    @Test
    void respond_overdoseKeyword_forcesHighRiskAndEscalate() {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("I think I overdosed on something");

        String aiJson = """
                {
                  "riskLevel": "LOW",
                  "message": "Call 911 immediately.",
                  "steps": ["Call 911 now"],
                  "script": "Help me.",
                  "escalate": false
                }
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CrisisResponseDto result = crisisService.respond(req, testUser);

        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
        assertThat(result.isEscalate()).isTrue();
    }

    @Test
    void respond_wantToDieKeyword_forcesHighRiskAndEscalate() {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("I want to die, I can't take this anymore");

        String aiJson = """
                {
                  "riskLevel": "MEDIUM",
                  "message": "Please reach out immediately.",
                  "steps": ["Call 988"],
                  "script": "I need help.",
                  "escalate": false
                }
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CrisisResponseDto result = crisisService.respond(req, testUser);

        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
        assertThat(result.isEscalate()).isTrue();
    }

    @Test
    void respond_killMyselfKeyword_forcesHighRiskAndEscalate() {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("I feel like I want to kill myself");

        String aiJson = """
                {"riskLevel": "LOW", "message": "Help.", "steps": [], "script": "", "escalate": false}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CrisisResponseDto result = crisisService.respond(req, testUser);

        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
        assertThat(result.isEscalate()).isTrue();
    }

    @Test
    void respond_cantBreatheKeyword_forcesHighRiskAndEscalate() {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("I can't breathe properly");

        String aiJson = """
                {"riskLevel": "MEDIUM", "message": "Call 911.", "steps": [], "script": "", "escalate": false}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CrisisResponseDto result = crisisService.respond(req, testUser);

        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
        assertThat(result.isEscalate()).isTrue();
    }

    @Test
    void respond_attachesPrimaryContactFromUser() {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("Feeling a bit down");

        String aiJson = """
                {"riskLevel": "LOW", "message": "You are not alone.", "steps": [], "script": "", "escalate": false}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CrisisResponseDto result = crisisService.respond(req, testUser);

        assertThat(result.getContactName()).isEqualTo("Mom");
        assertThat(result.getContactPhone()).isEqualTo("555-1234");
    }

    @Test
    void respond_aiParseFailure_usesDefaultValues() {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("I am struggling");

        when(geminiAiService.callGemini(anyString())).thenReturn("invalid-json");
        when(geminiAiService.extractJson(anyString())).thenReturn("invalid-json");

        CrisisResponseDto result = crisisService.respond(req, testUser);

        assertThat(result.getMessage()).isNotBlank();
        assertThat(result.getRiskLevel()).isNotBlank();
    }

    @Test
    void respond_ttsTextEqualsMessage() {
        CrisisRequest req = new CrisisRequest();
        req.setInputText("Feeling anxious");

        String aiJson = """
                {"riskLevel": "MEDIUM", "message": "Breathe deeply.", "steps": [], "script": "Help me.", "escalate": false}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CrisisResponseDto result = crisisService.respond(req, testUser);

        assertThat(result.getTtsText()).isEqualTo(result.getMessage());
    }
}
