package com.recoverease.repository;

import com.recoverease.entity.ResourceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceItem, Long> {
    List<ResourceItem> findByCategory(String category);
    List<ResourceItem> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
            String title, String summary);
}
