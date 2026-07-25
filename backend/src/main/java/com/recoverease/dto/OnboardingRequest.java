package com.recoverease.dto;

import lombok.Data;

@Data
public class OnboardingRequest {
    private String triggers;
    private String calmingStrategies;
    private String warningSignsPersonal;
    private String personalReminder;
    private String primaryContactName;
    private String primaryContactPhone;
    private String primaryContactRelation;
    private boolean consentToAlert;
    private String preferredLanguage;
}
