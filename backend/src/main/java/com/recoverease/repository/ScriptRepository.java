package com.recoverease.repository;

import com.recoverease.entity.GeneratedScript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScriptRepository extends JpaRepository<GeneratedScript, Long> {
    List<GeneratedScript> findByUserIdOrderByCreatedAtDesc(Long userId);
}
