package com.recoverease.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "generated_scripts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedScript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String scenario;
    private String audience;

    @Column(columnDefinition = "TEXT")
    private String scriptText;

    private LocalDateTime createdAt = LocalDateTime.now();
}
