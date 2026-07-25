package com.recoverease.repository;

import com.recoverease.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<CheckIn> findTop7ByUserIdOrderByCreatedAtDesc(Long userId);
}
