package com.recoverease.controller;

import com.recoverease.dto.CrisisRequest;
import com.recoverease.dto.CrisisResponseDto;
import com.recoverease.entity.User;
import com.recoverease.service.CrisisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crisis")
@RequiredArgsConstructor
public class CrisisController {

    private final CrisisService crisisService;

    @PostMapping("/respond")
    public ResponseEntity<CrisisResponseDto> respond(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CrisisRequest req) {
        CrisisResponseDto response = crisisService.respond(req, user);
        return ResponseEntity.ok(response);
    }
}
