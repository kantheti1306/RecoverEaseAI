package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.ScriptRequest;
import com.recoverease.entity.GeneratedScript;
import com.recoverease.entity.User;
import com.recoverease.repository.ScriptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScriptServiceTest {

    @Mock
    private GeminiAiService geminiAiService;

    @Mock
    private ScriptRepository scriptRepository;

    @InjectMocks
    private ScriptService scriptService;

    private User testUser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        try {
            var field = ScriptService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(scriptService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Sam");
        testUser.setEmail("sam@example.com");
        testUser.setRole("INDIVIDUAL");
        testUser.setPrimaryContactName("Dad");
        testUser.setCalmingStrategies("walking");
    }

    @Test
    void generateScript_success_returnsTitleAndScript() {
        String aiJson = """
                {
                  "title": "Craving Script for Self",
                  "script": "I acknowledge this craving and know it will pass."
                }
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(scriptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScriptRequest req = new ScriptRequest();
        req.setScenario("craving");
        req.setAudience("self");

        Map<String, String> result = scriptService.generateScript(req, testUser);

        assertThat(result.get("title")).isEqualTo("Craving Script for Self");
        assertThat(result.get("script")).isEqualTo("I acknowledge this craving and know it will pass.");
    }

    @Test
    void generateScript_aiParseFailure_usesRawTextAndFallbackTitle() {
        String rawText = "I need help with cravings right now.";
        when(geminiAiService.callGemini(anyString())).thenReturn(rawText);
        when(geminiAiService.extractJson(anyString())).thenReturn("not-valid-json");
        when(scriptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScriptRequest req = new ScriptRequest();
        req.setScenario("craving");
        req.setAudience("self");

        Map<String, String> result = scriptService.generateScript(req, testUser);

        assertThat(result.get("script")).isNotBlank();
        assertThat(result.get("title")).isNotBlank();
    }

    @Test
    void generateScript_persistsToRepository() {
        String aiJson = """
                {"title": "Refusal Script", "script": "No thank you."}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(scriptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScriptRequest req = new ScriptRequest();
        req.setScenario("refusal");
        req.setAudience("support_person");

        scriptService.generateScript(req, testUser);

        verify(scriptRepository).save(argThat(gs ->
                gs.getUserId().equals(1L) &&
                "refusal".equals(gs.getScenario()) &&
                "support_person".equals(gs.getAudience())
        ));
    }

    @Test
    void generateScript_callsGeminiWithPromptContainingScenario() {
        String aiJson = """
                {"title": "Grounding Script", "script": "Feel the floor beneath you."}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);
        when(scriptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScriptRequest req = new ScriptRequest();
        req.setScenario("grounding");
        req.setAudience("caregiver");

        scriptService.generateScript(req, testUser);

        verify(geminiAiService).callGemini(argThat(prompt ->
                prompt.contains("grounding") && prompt.contains("caregiver")
        ));
    }
}
