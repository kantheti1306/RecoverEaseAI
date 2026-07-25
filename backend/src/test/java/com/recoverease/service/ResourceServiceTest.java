package com.recoverease.service;

import com.recoverease.entity.ResourceItem;
import com.recoverease.repository.ResourceRepository;
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
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private GeminiAiService geminiAiService;

    @InjectMocks
    private ResourceService resourceService;

    private ResourceItem buildResource(String title, String category) {
        ResourceItem item = new ResourceItem();
        item.setTitle(title);
        item.setCategory(category);
        return item;
    }

    @Test
    void getAllResources_returnsAllItems() {
        List<ResourceItem> items = List.of(
                buildResource("Understanding Addiction", "EDUCATION"),
                buildResource("SAMHSA Hotline", "HOTLINE")
        );
        when(resourceRepository.findAll()).thenReturn(items);

        List<ResourceItem> result = resourceService.getAllResources();

        assertThat(result).hasSize(2);
        verify(resourceRepository).findAll();
    }

    @Test
    void getByCategory_uppercasesAndFilters() {
        List<ResourceItem> items = List.of(buildResource("Addiction Facts", "EDUCATION"));
        when(resourceRepository.findByCategory("EDUCATION")).thenReturn(items);

        List<ResourceItem> result = resourceService.getByCategory("education");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("EDUCATION");
        verify(resourceRepository).findByCategory("EDUCATION");
    }

    @Test
    void search_delegatesToRepository() {
        List<ResourceItem> items = List.of(buildResource("Recovery Steps", "EDUCATION"));
        when(resourceRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
                "recovery", "recovery")).thenReturn(items);

        List<ResourceItem> result = resourceService.search("recovery");

        assertThat(result).hasSize(1);
    }

    @Test
    void explainTopic_success_returnsAnswerAndSource() {
        String aiJson = """
                {"answer": "Addiction is a chronic condition.", "source": "SAMHSA"}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        Map<String, String> result = resourceService.explainTopic("What is addiction?");

        assertThat(result.get("answer")).isEqualTo("Addiction is a chronic condition.");
        assertThat(result.get("source")).isEqualTo("SAMHSA");
    }

    @Test
    void explainTopic_aiParseFailure_returnsRawResponseWithDefaultSource() {
        String rawText = "Addiction is complex.";
        when(geminiAiService.callGemini(anyString())).thenReturn(rawText);
        when(geminiAiService.extractJson(anyString())).thenReturn("not-json");

        Map<String, String> result = resourceService.explainTopic("What is addiction?");

        assertThat(result.get("answer")).isNotBlank();
        assertThat(result.get("source")).isEqualTo("SAMHSA / NIDA");
    }

    @Test
    void explainTopic_promptContainsQuestion() {
        String aiJson = """
                {"answer": "Recovery takes time.", "source": "NIDA"}
                """;
        when(geminiAiService.callGemini(anyString())).thenReturn(aiJson);
        when(geminiAiService.extractJson(anyString())).thenReturn(aiJson);

        resourceService.explainTopic("How long does recovery take?");

        verify(geminiAiService).callGemini(argThat(p ->
                p.contains("How long does recovery take?")
        ));
    }
}
