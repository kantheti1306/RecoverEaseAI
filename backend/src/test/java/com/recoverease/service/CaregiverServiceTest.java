package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.CaregiverRequest;
import com.recoverease.dto.CaregiverResponseDto;
import com.recoverease.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaregiverServiceTest {

    @Mock
    private GeminiAiService geminiAiService;

    @InjectMocks
    private CaregiverService caregiverService;

    private User caregiver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Inject ObjectMapper since it is a final field, not a mock
        try {
            var field = CaregiverService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(caregiverService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        caregiver = new User();
        caregiver.setId(1L);
        caregiver.setName("Carol");
        caregiver.setEmail("carol@example.com");
        caregiver.setRole("CAREGIVER");
    }

    // ─── Happy-path: valid scenario with AI response ──────────────────────────

    @Test
    void getGuidance_knownScenario_returnsPopulatedDto() {
        CaregiverRequest req = buildRequest("anxious", null);

        String aiJson = """
                {
                  "whatToSay": "I'm here with you. Let's breathe together.",
                  "avoidSaying": ["Why can't you calm down?", "You're overreacting"],
                  "nextSteps": ["Sit with them", "Offer water", "Call a professional"]
                }
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        assertThat(result).isNotNull();
        assertThat(result.getWhatToSay()).isEqualTo("I'm here with you. Let's breathe together.");
        assertThat(result.getAvoidSaying()).hasSize(2);
        assertThat(result.getNextSteps()).hasSize(3);
        assertThat(result.getScenarioLabel()).isEqualTo("Person appears anxious or panicked");
    }

    @Test
    void getGuidance_overdoseScenario_setsCorrectLabel() {
        CaregiverRequest req = buildRequest("overdose_concern", null);

        String aiJson = """
                {
                  "whatToSay": "I'm calling 911 right now. Stay with me.",
                  "avoidSaying": ["Calm down"],
                  "nextSteps": ["Call 911", "Administer naloxone", "Keep them awake"]
                }
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        assertThat(result.getScenarioLabel()).isEqualTo("Suspected overdose - EMERGENCY");
    }

    @Test
    void getGuidance_angryScenario_setsCorrectLabel() {
        CaregiverRequest req = buildRequest("angry", "He threw things around the room");

        String aiJson = """
                {
                  "whatToSay": "I understand you're angry. I'm not going anywhere.",
                  "avoidSaying": ["Stop it"],
                  "nextSteps": ["Give space", "Stay calm", "De-escalate"]
                }
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        assertThat(result.getScenarioLabel()).isEqualTo("Person is agitated or showing anger");
    }

    @Test
    void getGuidance_possibleRelapseScenario_setsCorrectLabel() {
        CaregiverRequest req = buildRequest("possible_relapse", null);

        String aiJson = """
                {"whatToSay": "I'm here.", "avoidSaying": ["Shame"], "nextSteps": ["Listen", "Support"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        assertThat(result.getScenarioLabel()).isEqualTo("Possible relapse situation");
    }

    @Test
    void getGuidance_withdrawnScenario_setsCorrectLabel() {
        CaregiverRequest req = buildRequest("withdrawn", null);

        String aiJson = """
                {"whatToSay": "I'm here.", "avoidSaying": ["Come out"], "nextSteps": ["Be patient"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        assertThat(result.getScenarioLabel()).isEqualTo("Person is withdrawn or isolating");
    }

    // ─── Unknown scenario falls back to raw scenario string ──────────────────

    @Test
    void getGuidance_unknownScenario_usesRawScenarioAsLabel() {
        CaregiverRequest req = buildRequest("unknown_scenario", null);

        String aiJson = """
                {"whatToSay": "Stay calm.", "avoidSaying": ["Don't"], "nextSteps": ["Wait"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        assertThat(result.getScenarioLabel()).isEqualTo("unknown_scenario");
    }

    // ─── Context is passed to Gemini prompt ──────────────────────────────────

    @Test
    void getGuidance_withContext_includesContextInPrompt() {
        CaregiverRequest req = buildRequest("anxious", "She hasn't slept in two days");

        String aiJson = """
                {"whatToSay": "Rest.", "avoidSaying": ["Sleep!"], "nextSteps": ["Sit with her"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        caregiverService.getGuidance(req, caregiver);

        verify(geminiAiService).callGemini(argThat(prompt ->
                prompt.contains("She hasn't slept in two days")
        ));
    }

    @Test
    void getGuidance_nullContext_usesDefaultContextText() {
        CaregiverRequest req = buildRequest("anxious", null);

        String aiJson = """
                {"whatToSay": "I'm here.", "avoidSaying": [], "nextSteps": []}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        caregiverService.getGuidance(req, caregiver);

        verify(geminiAiService).callGemini(argThat(prompt ->
                prompt.contains("No additional context provided")
        ));
    }

    // ─── AI parse failure → defaults ─────────────────────────────────────────

    @Test
    void getGuidance_aiParseFailure_returnsDefaultValues() {
        CaregiverRequest req = buildRequest("anxious", null);

        when(geminiAiService.callGemini(anyString())).thenReturn("not-valid-json");
        when(geminiAiService.extractJson(anyString())).thenReturn("not-valid-json");

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        assertThat(result.getWhatToSay()).isNotBlank();
        assertThat(result.getAvoidSaying()).isNotEmpty();
        assertThat(result.getNextSteps()).isNotEmpty();
    }

    @Test
    void getGuidance_overdoseScenario_parseFailure_returnsEmergencyDefault() {
        CaregiverRequest req = buildRequest("overdose_concern", null);

        when(geminiAiService.callGemini(anyString())).thenReturn("bad-json");
        when(geminiAiService.extractJson(anyString())).thenReturn("bad-json");

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        // The overdose fallback nextSteps include "Call 911 immediately"
        assertThat(result.getNextSteps()).anyMatch(s -> s.contains("911") || s.toLowerCase().contains("naloxone"));
        // whatToSay is non-blank and expresses care
        assertThat(result.getWhatToSay()).isNotBlank();
    }

    @Test
    void getGuidance_nonOverdoseParseFailure_returnsGenericDefault() {
        CaregiverRequest req = buildRequest("anxious", null);

        when(geminiAiService.callGemini(anyString())).thenReturn("bad-json");
        when(geminiAiService.extractJson(anyString())).thenReturn("bad-json");

        CaregiverResponseDto result = caregiverService.getGuidance(req, caregiver);

        // Default non-overdose avoid list
        assertThat(result.getAvoidSaying()).contains("'Why can't you just stop?'");
    }

    // ─── Prompt contains caregiver name ──────────────────────────────────────

    @Test
    void getGuidance_promptIncludesCaregiverName() {
        CaregiverRequest req = buildRequest("angry", null);

        String aiJson = """
                {"whatToSay": "Stay calm.", "avoidSaying": [], "nextSteps": []}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        caregiverService.getGuidance(req, caregiver);

        verify(geminiAiService).callGemini(argThat(prompt ->
                prompt.contains("Carol")
        ));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private CaregiverRequest buildRequest(String scenario, String context) {
        CaregiverRequest req = new CaregiverRequest();
        req.setScenario(scenario);
        req.setContext(context);
        return req;
    }
}
