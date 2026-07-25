package com.recoverease.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScriptRequest {
    @NotBlank
    private String scenario; // craving, relapse_risk, refusal, grounding, caregiver
    @NotBlank
    private String audience; // self, caregiver, support_person
}
