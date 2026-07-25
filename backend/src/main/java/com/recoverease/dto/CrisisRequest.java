package com.recoverease.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrisisRequest {
    @NotBlank
    private String inputText;
    private String mode = "text"; // "voice" or "text"
}
