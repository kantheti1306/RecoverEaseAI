package com.recoverease.controller;

import com.recoverease.entity.ResourceItem;
import com.recoverease.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<List<ResourceItem>> getAllResources(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(resourceService.search(search));
        }
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(resourceService.getByCategory(category));
        }
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @PostMapping("/explain")
    public ResponseEntity<Map<String, String>> explainTopic(
            @RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        if (question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }
        return ResponseEntity.ok(resourceService.explainTopic(question));
    }
}
