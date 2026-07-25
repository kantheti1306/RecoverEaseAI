package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.ScriptRequest;
import com.recoverease.entity.GeneratedScript;
import com.recoverease.entity.User;
import com.recoverease.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptService {

    private final GeminiAiService geminiAiService;
    private final ScriptRepository scriptRepository;
    private final ObjectMapper objectMapper;

    public Map<String, String> generateScript(ScriptRequest req, User user) {
        String prompt = buildScriptPrompt(req.getScenario(), req.getAudience(), user);
        String rawResponse = geminiAiService.callGemini(prompt);
        String jsonStr = geminiAiService.extractJson(rawResponse);

        String title = "";
        String script = "";

        try {
            var node = objectMapper.readTree(jsonStr);
            title = node.path("title").asText("Support Script");
            script = node.path("script").asText(rawResponse);
        } catch (Exception e) {
            log.error("Script parse error: {}", e.getMessage());
            title = toTitle(req.getScenario(), req.getAudience());
            script = rawResponse;
        }

        // Persist to DB
        GeneratedScript gs = new GeneratedScript();
        gs.setUserId(user.getId());
        gs.setScenario(req.getScenario());
        gs.setAudience(req.getAudience());
        gs.setScriptText(script);
        scriptRepository.save(gs);

        return Map.of("title", title, "script", script);
    }

    private String buildScriptPrompt(String scenario, String audience, User user) {
        String name = user.getName();
        String contact = user.getPrimaryContactName() != null ? user.getPrimaryContactName() : "a trusted person";
        String calming = user.getCalmingStrategies() != null ? user.getCalmingStrategies() : "breathing exercises";

        return String.format("""
You are a compassionate recovery support script writer for RecoverEase AI.
Write a short, practical, emotionally intelligent script for the given scenario and audience.
Keep it under 80 words. Use plain, warm language.
Do not include diagnosis or medical advice.

User name: %s
Primary support contact: %s
Calming strategy: %s
Scenario: %s
Audience: %s (who will use or read this script)

Return ONLY valid JSON (no markdown):
{
  "title": "Brief descriptive title",
  "script": "The full script text ready to speak or share"
}
""", name, contact, calming, scenario, audience);
    }

    private String toTitle(String scenario, String audience) {
        return scenario.replace("_", " ").substring(0, 1).toUpperCase()
                + scenario.replace("_", " ").substring(1) + " Script for " + audience;
    }
}
