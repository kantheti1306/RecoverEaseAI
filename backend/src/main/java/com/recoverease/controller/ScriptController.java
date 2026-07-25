package com.recoverease.controller;

import com.recoverease.dto.ScriptRequest;
import com.recoverease.entity.GeneratedScript;
import com.recoverease.entity.User;
import com.recoverease.repository.ScriptRepository;
import com.recoverease.service.ScriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scripts")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;
    private final ScriptRepository scriptRepository;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateScript(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ScriptRequest req) {
        Map<String, String> result = scriptService.generateScript(req, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<GeneratedScript>> getHistory(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(scriptRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }
}
