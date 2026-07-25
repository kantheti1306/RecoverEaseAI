package com.recoverease.service;

import com.recoverease.entity.ResourceItem;
import com.recoverease.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final GeminiAiService geminiAiService;

    public List<ResourceItem> getAllResources() {
        return resourceRepository.findAll();
    }

    public List<ResourceItem> getByCategory(String category) {
        return resourceRepository.findByCategory(category.toUpperCase());
    }

    public List<ResourceItem> search(String query) {
        return resourceRepository
                .findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(query, query);
    }

    /**
     * Real AI call: explain a resource topic in plain language
     */
    public Map<String, String> explainTopic(String question) {
        String prompt = String.format("""
You are an educational recovery support assistant for RecoverEase AI.
Answer the following recovery-related question in simple, warm, plain language.
Keep the answer under 120 words.
Reference trusted organizations (SAMHSA, NIDA, CDC, WHO) where appropriate.
Do NOT diagnose or recommend specific medications.
End with a brief supportive statement.

Question: %s

Return ONLY valid JSON (no markdown):
{
  "answer": "Your plain-language answer here",
  "source": "Primary trusted source cited"
}
""", question);

        String rawResponse = geminiAiService.callGemini(prompt);
        String jsonStr = geminiAiService.extractJson(rawResponse);

        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonStr);
            return Map.of(
                    "answer", node.path("answer").asText(rawResponse),
                    "source", node.path("source").asText("SAMHSA / NIDA")
            );
        } catch (Exception e) {
            log.error("Resource explain parse error: {}", e.getMessage());
            return Map.of("answer", rawResponse, "source", "SAMHSA / NIDA");
        }
    }
}
