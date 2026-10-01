package com.example.aicontentdetector.controller;

import com.example.aicontentdetector.model.DetectionResult;
import com.example.aicontentdetector.service.DetectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final DetectionService detectionService;

    public TestController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping("/test")
    public DetectionResult test() {
        return detectionService.detect();
    }
}