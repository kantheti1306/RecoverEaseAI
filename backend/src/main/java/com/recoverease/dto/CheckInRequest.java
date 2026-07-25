package com.recoverease.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequest {
    @NotBlank
    private String mood;

    @Min(1) @Max(10)
    private int cravingLevel;

    @Min(1) @Max(10)
    private int stressLevel;

    @Min(1) @Max(10)
    private int sleepQuality;

    private String voiceNote;
}
