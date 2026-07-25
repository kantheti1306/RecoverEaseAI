package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverease.dto.CheckInRequest;
import com.recoverease.entity.CheckIn;
import com.recoverease.entity.User;
import com.recoverease.repository.CheckInRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInService {

    private final GeminiAiService geminiAiService;
    private final CheckInRepository checkInRepository;
    private final ObjectMapper objectMapper;

    public Map<String, Object> submitCheckIn(CheckInRequest req, User user) {
        // Rule-based risk scoring (fast, reliable)
        String riskLevel = calculateRisk(req);

        // AI-powered personalized summary and suggestions
        String prompt = buildCheckInPrompt(req, user, riskLevel);
        String rawResponse = geminiAiService.callGemini(prompt);
        String jsonStr = geminiAiService.extractJson(rawResponse);

        String aiSummary = "Your check-in has been recorded.";
        List<String> suggestions = List.of("Practice a grounding technique", "Reach out to your support person");

        try {
            var node = objectMapper.readTree(jsonStr);
            aiSummary = node.path("summary").asText(aiSummary);
            var suggestionsNode = node.path("suggestions");
            if (suggestionsNode.isArray()) {
                suggestions = objectMapper.convertValue(suggestionsNode,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
        } catch (Exception e) {
            log.error("CheckIn AI parse error: {}", e.getMessage());
        }

        // Save to DB
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(user.getId());
        checkIn.setMood(req.getMood());
        checkIn.setCravingLevel(req.getCravingLevel());
        checkIn.setStressLevel(req.getStressLevel());
        checkIn.setSleepQuality(req.getSleepQuality());
        checkIn.setVoiceNote(req.getVoiceNote());
        checkIn.setRiskLevel(riskLevel);
        checkIn.setAiSummary(aiSummary);
        checkIn.setSuggestions(String.join("|", suggestions));
        checkInRepository.save(checkIn);

        return Map.of(
                "riskLevel", riskLevel,
                "summary", aiSummary,
                "suggestions", suggestions
        );
    }

    public List<CheckIn> getHistory(Long userId) {
        return checkInRepository.findTop7ByUserIdOrderByCreatedAtDesc(userId);
    }

    private String calculateRisk(CheckInRequest req) {
        int score = 0;
        if (req.getCravingLevel() >= 7) score += 3;
        else if (req.getCravingLevel() >= 4) score += 1;

        if (req.getStressLevel() >= 7) score += 2;
        else if (req.getStressLevel() >= 4) score += 1;

        if (req.getSleepQuality() <= 3) score += 2;
        else if (req.getSleepQuality() <= 5) score += 1;

        if (req.getMood() != null) {
            String mood = req.getMood().toLowerCase();
            if (mood.equals("very_distressed") || mood.equals("overwhelmed")) score += 2;
            else if (mood.equals("anxious") || mood.equals("depressed")) score += 1;
        }

        if (req.getVoiceNote() != null) {
            String note = req.getVoiceNote().toLowerCase();
            if (note.contains("relapse") || note.contains("using") || note.contains("can't cope")) score += 3;
            else if (note.contains("tempted") || note.contains("struggling")) score += 1;
        }

        if (score >= 6) return "HIGH";
        if (score >= 3) return "MEDIUM";
        return "LOW";
    }

    private String buildCheckInPrompt(CheckInRequest req, User user, String riskLevel) {
        return String.format("""
You are a recovery wellness coach for RecoverEase AI.
Based on the user's daily check-in, provide a brief, empathetic summary and 3 actionable suggestions.
Do NOT diagnose. Keep language warm and supportive.

User name: %s
Today's mood: %s
Craving level (1-10): %d
Stress level (1-10): %d
Sleep quality (1-10): %d
Voice note: "%s"
Calculated risk level: %s

Return ONLY valid JSON (no markdown):
{
  "summary": "Personalized 2-sentence summary (warm, non-judgmental, max 60 words)",
  "suggestions": ["suggestion 1", "suggestion 2", "suggestion 3"]
}
""", user.getName(), req.getMood(), req.getCravingLevel(),
             req.getStressLevel(), req.getSleepQuality(),
             req.getVoiceNote() != null ? req.getVoiceNote() : "none",
             riskLevel);
    }
}
