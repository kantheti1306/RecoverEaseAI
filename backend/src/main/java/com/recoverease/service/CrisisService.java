package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.CrisisRequest;
import com.recoverease.dto.CrisisResponseDto;
import com.recoverease.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrisisService {

    private final GeminiAiService geminiAiService;
    private final ObjectMapper objectMapper;

    public CrisisResponseDto respond(CrisisRequest req, User user) {
        String inputLower = req.getInputText().toLowerCase();

        // Pre-emptive escalation for life-threatening inputs
        boolean isEmergency = inputLower.contains("overdose") || inputLower.contains("overdosed")
                || inputLower.contains("can't breathe") || inputLower.contains("cannot breathe")
                || (inputLower.contains("want to die") || inputLower.contains("kill myself"));

        String prompt = buildCrisisPrompt(req.getInputText(), user, isEmergency);
        String rawResponse = geminiAiService.callGemini(prompt);
        String jsonStr = geminiAiService.extractJson(rawResponse);

        CrisisResponseDto dto = parseAiResponse(jsonStr);

        if (isEmergency) {
            dto.setRiskLevel("HIGH");
            dto.setEscalate(true);
        }

        // Attach user's primary contact
        dto.setContactName(user.getPrimaryContactName());
        dto.setContactPhone(user.getPrimaryContactPhone());

        // TTS text = just the message
        dto.setTtsText(dto.getMessage());

        return dto;
    }

    private String buildCrisisPrompt(String input, User user, boolean isEmergency) {
        String triggers = user.getTriggers() != null ? user.getTriggers() : "not specified";
        String calming = user.getCalmingStrategies() != null ? user.getCalmingStrategies() : "deep breathing, calling support";
        String contact = user.getPrimaryContactName() != null ? user.getPrimaryContactName() : "a trusted person";
        String reminder = user.getPersonalReminder() != null ? user.getPersonalReminder() : "You have come a long way. You are stronger than this moment.";

        return String.format("""
You are a trauma-informed, compassionate recovery support assistant for a substance use recovery platform called RecoverEase AI.
Your role is to provide brief, calm, grounding, and practical support.
IMPORTANT RULES:
- Do NOT diagnose medical conditions
- Do NOT give medication or withdrawal instructions
- If overdose or immediate physical danger is detected, ALWAYS recommend calling emergency services (911)
- Be non-judgmental, warm, and brief
- Keep the message under 60 words
- Keep each step under 15 words

User profile:
- Triggers: %s
- Calming strategies: %s
- Support contact: %s
- Personal reminder: "%s"

Emergency detected: %s

Current user input:
"%s"

Return ONLY valid JSON (no markdown, no explanation):
{
  "riskLevel": "LOW or MEDIUM or HIGH",
  "message": "short calming message (max 60 words)",
  "steps": ["step 1 (max 15 words)", "step 2", "step 3"],
  "script": "short personalized emergency support script (max 50 words)",
  "escalate": true or false
}
""", triggers, calming, contact, reminder, isEmergency, input);
    }

    private CrisisResponseDto parseAiResponse(String jsonStr) {
        CrisisResponseDto dto = new CrisisResponseDto();
        try {
            var node = objectMapper.readTree(jsonStr);
            dto.setRiskLevel(node.path("riskLevel").asText("MEDIUM"));
            dto.setMessage(node.path("message").asText("You are not alone. Take one breath at a time."));
            dto.setScript(node.path("script").asText("I need support right now. Please stay with me."));
            dto.setEscalate(node.path("escalate").asBoolean(false));

            var stepsNode = node.path("steps");
            if (stepsNode.isArray()) {
                List<String> steps = objectMapper.convertValue(stepsNode,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                dto.setSteps(steps);
            } else {
                dto.setSteps(List.of("Take 5 slow breaths", "Move to a safe place", "Contact your support person"));
            }
        } catch (Exception e) {
            log.error("Failed to parse crisis AI response: {}", e.getMessage());
            dto.setRiskLevel("MEDIUM");
            dto.setMessage("You are not alone. Take one breath at a time. This moment will pass.");
            dto.setSteps(Arrays.asList("Take 5 slow deep breaths", "Move away from the trigger", "Call your support contact now"));
            dto.setScript("I am struggling right now and I need your support. Please stay with me for a few minutes.");
            dto.setEscalate(false);
        }
        return dto;
    }
}
