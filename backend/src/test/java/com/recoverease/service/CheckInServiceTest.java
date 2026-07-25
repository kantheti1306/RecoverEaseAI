package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.CheckInRequest;
import com.recoverease.entity.CheckIn;
import com.recoverease.entity.User;
import com.recoverease.repository.CheckInRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private GeminiAiService geminiAiService;

    @Mock
    private CheckInRepository checkInRepository;

    @InjectMocks
    private CheckInService checkInService;

    private User testUser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Inject ObjectMapper via field since it's not a mock
        try {
            var field = CheckInService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(checkInService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole("INDIVIDUAL");
    }

    // ===== Risk Level Calculation Tests (via submitCheckIn) =====

    @Test
    void submitCheckIn_lowRisk_returnsLowRiskLevel() {
        CheckInRequest req = buildRequest("calm", 2, 2, 8, null);
        String aiJson = """
                {"summary": "Great check-in!", "suggestions": ["Keep it up"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(checkInRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = checkInService.submitCheckIn(req, testUser);

        assertThat(result.get("riskLevel")).isEqualTo("LOW");
    }

    @Test
    void submitCheckIn_highCravingHighStress_returnsHighRiskLevel() {
        CheckInRequest req = buildRequest("anxious", 9, 8, 2, null);
        String aiJson = """
                {"summary": "This is tough.", "suggestions": ["Call support", "Breathe"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(checkInRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = checkInService.submitCheckIn(req, testUser);

        assertThat(result.get("riskLevel")).isEqualTo("HIGH");
    }

    @Test
    void submitCheckIn_mediumRisk_returnsMediumRiskLevel() {
        CheckInRequest req = buildRequest("anxious", 5, 5, 5, null);
        String aiJson = """
                {"summary": "Moderate check-in.", "suggestions": ["Practice grounding"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(checkInRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = checkInService.submitCheckIn(req, testUser);

        assertThat(result.get("riskLevel")).isEqualTo("MEDIUM");
    }

    @Test
    void submitCheckIn_voiceNoteContainsRelapse_addsToHighRisk() {
        CheckInRequest req = buildRequest("calm", 3, 3, 7, "I am thinking about relapse");
        String aiJson = """
                {"summary": "Concern noted.", "suggestions": ["Reach out now"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(checkInRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = checkInService.submitCheckIn(req, testUser);

        // craving=3(0), stress=3(0), sleep=7(0), note=relapse(+3) => score=3 => MEDIUM
        assertThat(result.get("riskLevel")).isEqualTo("MEDIUM");
    }

    @Test
    void submitCheckIn_overwhelmedMood_increasesRisk() {
        // overwhelmed mood => score += 2; craving=3(0), stress=3(0), sleep=7(0) → score=2 → LOW
        // To reach MEDIUM(3) we need one more point, e.g. stress=4 => +1 => score=3 => MEDIUM
        CheckInRequest req = buildRequest("overwhelmed", 3, 5, 7, null);
        String aiJson = """
                {"summary": "Hard day.", "suggestions": ["Take a break"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(checkInRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = checkInService.submitCheckIn(req, testUser);

        // overwhelmed(+2) + stress=5(+1) = 3 => MEDIUM
        assertThat(result.get("riskLevel")).isEqualTo("MEDIUM");
    }

    @Test
    void submitCheckIn_savesPersistsCheckIn() {
        CheckInRequest req = buildRequest("calm", 2, 2, 8, null);
        String aiJson = """
                {"summary": "Good day!", "suggestions": ["Stay consistent"]}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(checkInRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        checkInService.submitCheckIn(req, testUser);

        verify(checkInRepository).save(argThat(ci ->
                ci.getUserId().equals(1L) &&
                "calm".equals(ci.getMood()) &&
                ci.getCravingLevel() == 2
        ));
    }

    @Test
    void submitCheckIn_aiParseFailure_usesDefaults() {
        CheckInRequest req = buildRequest("calm", 2, 2, 8, null);
        when(geminiAiService.callGemini(anyString())).thenReturn("not-json");
        when(geminiAiService.extractJson(anyString())).thenReturn("not-json");
        when(checkInRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = checkInService.submitCheckIn(req, testUser);

        assertThat(result.get("summary")).isEqualTo("Your check-in has been recorded.");
        assertThat(result.get("suggestions")).isNotNull();
    }

    @Test
    void getHistory_returnsCheckInsForUser() {
        CheckIn c1 = new CheckIn();
        c1.setUserId(1L);
        c1.setMood("calm");

        when(checkInRepository.findTop7ByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(c1));

        List<CheckIn> history = checkInService.getHistory(1L);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getMood()).isEqualTo("calm");
    }

    // ===== Helpers =====

    private CheckInRequest buildRequest(String mood, int craving, int stress, int sleep, String voiceNote) {
        CheckInRequest req = new CheckInRequest();
        req.setMood(mood);
        req.setCravingLevel(craving);
        req.setStressLevel(stress);
        req.setSleepQuality(sleep);
        req.setVoiceNote(voiceNote);
        return req;
    }
}
