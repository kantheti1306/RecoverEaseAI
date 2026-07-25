package com.recoverease.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role; // INDIVIDUAL or CAREGIVER

    private String preferredLanguage = "en";
    private boolean onboardingComplete = false;

    @Column(columnDefinition = "TEXT")
    private String triggers;

    @Column(columnDefinition = "TEXT")
    private String calmingStrategies;

    @Column(columnDefinition = "TEXT")
    private String warningSignsPersonal;

    @Column(columnDefinition = "TEXT")
    private String personalReminder;

    private String primaryContactName;
    private String primaryContactPhone;
    private String primaryContactRelation;
    private boolean consentToAlert = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
