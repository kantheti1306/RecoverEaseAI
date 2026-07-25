package com.recoverease.dto;

import lombok.Data;
import java.util.List;

@Data
public class CaregiverResponseDto {
    private String whatToSay;
    private List<String> avoidSaying;
    private List<String> nextSteps;
    private String scenarioLabel;
}
