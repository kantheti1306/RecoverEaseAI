package com.recoverease.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaregiverRequest {
    @NotBlank
    private String scenario; // anxious, angry, possible_relapse, withdrawn, overdose_concern
    private String context;  // optional additional context
}
