package com.recoverease.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class GeminiAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiAiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Calls Gemini API with a prompt and returns the text response.
     */
    public String callGemini(String prompt) {
        try {
            String url = apiUrl + "?key=" + apiKey;

            Map<String, Object> body = Map.of(
                "contents", new Object[]{
                    Map.of("parts", new Object[]{
                        Map.of("text", prompt)
                    })
                },
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 1024
                )
            );

            String response = webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // Extract text from Gemini response
            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates")
                       .path(0)
                       .path("content")
                       .path("parts")
                       .path(0)
                       .path("text")
                       .asText();

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            return getFallbackResponse();
        }
    }

    /**
     * Extract JSON from a Gemini response that may have markdown fences.
     */
    public String extractJson(String raw) {
        if (raw == null) return "{}";
        // Remove markdown code fences if present
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("```json", "").replaceAll("```", "").trim();
        }
        // Find first { and last }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end >= 0) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private String getFallbackResponse() {
        return "{\"error\": \"AI service temporarily unavailable. Please try again.\"}";
    }
}
