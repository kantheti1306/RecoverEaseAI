package com.recoverease.controller;

import com.recoverease.dto.OnboardingRequest;
import com.recoverease.entity.User;
import com.recoverease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @PostMapping("/onboarding")
    public ResponseEntity<Map<String, Object>> completeOnboarding(
            @AuthenticationPrincipal User user,
            @RequestBody OnboardingRequest req) {

        user.setTriggers(req.getTriggers());
        user.setCalmingStrategies(req.getCalmingStrategies());
        user.setWarningSignsPersonal(req.getWarningSignsPersonal());
        user.setPersonalReminder(req.getPersonalReminder());
        user.setPrimaryContactName(req.getPrimaryContactName());
        user.setPrimaryContactPhone(req.getPrimaryContactPhone());
        user.setPrimaryContactRelation(req.getPrimaryContactRelation());
        user.setConsentToAlert(req.isConsentToAlert());
        if (req.getPreferredLanguage() != null) {
            user.setPreferredLanguage(req.getPreferredLanguage());
        }
        user.setOnboardingComplete(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Onboarding complete",
                "onboardingComplete", true
        ));
    }
}
