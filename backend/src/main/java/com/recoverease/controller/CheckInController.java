package com.recoverease.controller;

import com.recoverease.dto.CheckInRequest;
import com.recoverease.entity.CheckIn;
import com.recoverease.entity.User;
import com.recoverease.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitCheckIn(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CheckInRequest req) {
        Map<String, Object> result = checkInService.submitCheckIn(req, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CheckIn>> getHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checkInService.getHistory(user.getId()));
    }
}
