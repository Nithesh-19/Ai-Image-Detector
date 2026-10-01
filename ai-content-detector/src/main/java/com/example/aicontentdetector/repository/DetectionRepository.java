package com.example.aicontentdetector.repository;

import com.example.aicontentdetector.model.DetectionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetectionRepository extends JpaRepository<DetectionResult, Long> {
}