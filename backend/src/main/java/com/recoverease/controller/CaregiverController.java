package com.recoverease.controller;

import com.recoverease.dto.CaregiverRequest;
import com.recoverease.dto.CaregiverResponseDto;
import com.recoverease.entity.User;
import com.recoverease.service.CaregiverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caregiver")
@RequiredArgsConstructor
public class CaregiverController {

    private final CaregiverService caregiverService;

    @PostMapping("/guidance")
    public ResponseEntity<CaregiverResponseDto> getGuidance(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CaregiverRequest req) {
        CaregiverResponseDto result = caregiverService.getGuidance(req, user);
        return ResponseEntity.ok(result);
    }
}
