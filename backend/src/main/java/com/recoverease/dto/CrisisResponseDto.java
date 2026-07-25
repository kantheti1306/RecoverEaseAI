package com.recoverease.dto;

import lombok.Data;
import java.util.List;

@Data
public class CrisisResponseDto {
    private String riskLevel;
    private String message;
    private List<String> steps;
    private String script;
    private String ttsText;
    private boolean escalate;
    private String contactName;
    private String contactPhone;
}
