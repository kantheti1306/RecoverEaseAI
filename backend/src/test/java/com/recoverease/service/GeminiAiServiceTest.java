package com.recoverease.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiAiServiceTest {

    private GeminiAiService geminiAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        geminiAiService = new GeminiAiService(webClientBuilder, objectMapper);
        ReflectionTestUtils.setField(geminiAiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(geminiAiService, "apiUrl",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent");
    }

    // ─── extractJson tests (pure logic, no HTTP) ─────────────────────────────

    @Test
    void extractJson_plainJson_returnsAsIs() {
        String input = "{\"key\": \"value\"}";
        String result = geminiAiService.extractJson(input);
        assertThat(result).isEqualTo("{\"key\": \"value\"}");
    }

    @Test
    void extractJson_withMarkdownFences_stripsAndReturnsJson() {
        String input = "```json\n{\"answer\": \"hello\"}\n```";
        String result = geminiAiService.extractJson(input);
        assertThat(result).isEqualTo("{\"answer\": \"hello\"}");
    }

    @Test
    void extractJson_withPlainCodeFences_stripsAndReturnsJson() {
        String input = "```\n{\"riskLevel\": \"LOW\"}\n```";
        String result = geminiAiService.extractJson(input);
        assertThat(result).isEqualTo("{\"riskLevel\": \"LOW\"}");
    }

    @Test
    void extractJson_withLeadingText_extractsBraceContent() {
        String input = "Here is the response: {\"message\": \"ok\"} done.";
        String result = geminiAiService.extractJson(input);
        assertThat(result).isEqualTo("{\"message\": \"ok\"}");
    }

    @Test
    void extractJson_nullInput_returnsEmptyObject() {
        String result = geminiAiService.extractJson(null);
        assertThat(result).isEqualTo("{}");
    }

    @Test
    void extractJson_noBraces_returnsCleanedString() {
        String result = geminiAiService.extractJson("no braces here");
        assertThat(result).isEqualTo("no braces here");
    }

    @Test
    void extractJson_emptyString_returnsEmptyObject() {
        // Empty raw string after trim has no '{', returns cleaned string
        String result = geminiAiService.extractJson("  ");
        assertThat(result).isEmpty();
    }

    @Test
    void extractJson_nestedJson_keepsOutermostBraces() {
        String input = "{\"outer\": {\"inner\": \"val\"}}";
        String result = geminiAiService.extractJson(input);
        assertThat(result).isEqualTo("{\"outer\": {\"inner\": \"val\"}}");
    }

    // ─── callGemini: WebClient failure → fallback ─────────────────────────────

    @SuppressWarnings("unchecked")
    @Test
    void callGemini_webClientThrowsException_returnsFallback() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Network error")));

        String result = geminiAiService.callGemini("Tell me about recovery.");

        assertThat(result).contains("AI service temporarily unavailable");
    }

    @SuppressWarnings("unchecked")
    @Test
    void callGemini_successfulResponse_extractsTextFromCandidates() throws Exception {
        String geminiResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [{"text": "This is the AI response."}]
                      }
                    }
                  ]
                }
                """;

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(geminiResponse));

        String result = geminiAiService.callGemini("Some prompt");

        assertThat(result).isEqualTo("This is the AI response.");
    }

    @SuppressWarnings("unchecked")
    @Test
    void callGemini_malformedJsonResponse_returnsFallback() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("not-valid-json"));

        String result = geminiAiService.callGemini("Any prompt");

        assertThat(result).contains("AI service temporarily unavailable");
    }
}
