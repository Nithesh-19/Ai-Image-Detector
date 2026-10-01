package com.example.aicontentdetector.controller;

import com.example.aicontentdetector.model.DetectionResult;
import com.example.aicontentdetector.service.DetectionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(
            DetectionService detectionService) {

        this.detectionService = detectionService;
    }

    // ==========================================
    // IMAGE DETECTION
    // ==========================================

    @PostMapping("/detect")
    public ResponseEntity<?> detect(
            @RequestParam("file") MultipartFile file) {

        try {

            DetectionResult result =
                    detectionService.readImage(file);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Prediction error: "
                                    + e.getMessage()
                    );
        }
    }

    // ==========================================
    // DETECTION HISTORY
    // ==========================================

    @GetMapping("/history")
    public ResponseEntity<List<DetectionResult>> history() {

        List<DetectionResult> results =
                detectionService.getHistory();

        return ResponseEntity.ok(results);
    }
}