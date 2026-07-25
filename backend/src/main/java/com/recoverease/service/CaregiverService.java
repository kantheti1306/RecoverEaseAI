package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.CaregiverRequest;
import com.recoverease.dto.CaregiverResponseDto;
import com.recoverease.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaregiverService {

    private final GeminiAiService geminiAiService;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> SCENARIO_LABELS = Map.of(
            "anxious", "Person appears anxious or panicked",
            "angry", "Person is agitated or showing anger",
            "possible_relapse", "Possible relapse situation",
            "withdrawn", "Person is withdrawn or isolating",
            "overdose_concern", "Suspected overdose - EMERGENCY"
    );

    public CaregiverResponseDto getGuidance(CaregiverRequest req, User caregiver) {
        String scenarioLabel = SCENARIO_LABELS.getOrDefault(req.getScenario(), req.getScenario());
        String prompt = buildCaregiverPrompt(req, scenarioLabel, caregiver);
        String rawResponse = geminiAiService.callGemini(prompt);
        String jsonStr = geminiAiService.extractJson(rawResponse);

        return parseResponse(jsonStr, scenarioLabel, req.getScenario());
    }

    private String buildCaregiverPrompt(CaregiverRequest req, String scenarioLabel, User user) {
        return String.format("""
You are a trauma-informed caregiver support assistant for RecoverEase AI.
Provide compassionate, practical guidance for a caregiver supporting a loved one with substance use challenges.
Be non-judgmental, safety-focused, and concise.
Do NOT diagnose. Do NOT provide medical treatment instructions.
If overdose is suspected, ALWAYS recommend calling 911 immediately as the first step.

Caregiver name: %s
Scenario: %s
Additional context: %s

Return ONLY valid JSON (no markdown):
{
  "whatToSay": "Specific script of what to say (max 60 words, calm and compassionate)",
  "avoidSaying": ["thing to avoid 1", "thing to avoid 2", "thing to avoid 3"],
  "nextSteps": ["immediate action 1", "immediate action 2", "immediate action 3"]
}
""", user.getName(), scenarioLabel, req.getContext() != null ? req.getContext() : "No additional context provided");
    }

    private CaregiverResponseDto parseResponse(String jsonStr, String label, String scenario) {
        CaregiverResponseDto dto = new CaregiverResponseDto();
        dto.setScenarioLabel(label);
        try {
            var node = objectMapper.readTree(jsonStr);
            dto.setWhatToSay(node.path("whatToSay").asText(getDefaultScript(scenario)));

            var avoid = node.path("avoidSaying");
            if (avoid.isArray()) {
                dto.setAvoidSaying(objectMapper.convertValue(avoid,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            } else {
                dto.setAvoidSaying(getDefaultAvoid());
            }

            var steps = node.path("nextSteps");
            if (steps.isArray()) {
                dto.setNextSteps(objectMapper.convertValue(steps,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            } else {
                dto.setNextSteps(getDefaultSteps(scenario));
            }
        } catch (Exception e) {
            log.error("Caregiver AI parse error: {}", e.getMessage());
            dto.setWhatToSay(getDefaultScript(scenario));
            dto.setAvoidSaying(getDefaultAvoid());
            dto.setNextSteps(getDefaultSteps(scenario));
        }
        return dto;
    }

    private String getDefaultScript(String scenario) {
        if ("overdose_concern".equals(scenario))
            return "I'm calling for help right now. Stay with me. I love you and we are going to get through this together.";
        return "I'm here with you. I love you and I'm not going anywhere. Let's take this one moment at a time together.";
    }

    private List<String> getDefaultAvoid() {
        return List.of("'Why can't you just stop?'", "'You're being selfish'", "'I'm so disappointed in you'");
    }

    private List<String> getDefaultSteps(String scenario) {
        if ("overdose_concern".equals(scenario))
            return List.of("Call 911 immediately", "Administer naloxone if available", "Keep them awake and on their side");
        return List.of("Stay calm and present", "Use open, non-judgmental communication", "Offer one supportive action at a time");
    }
}
