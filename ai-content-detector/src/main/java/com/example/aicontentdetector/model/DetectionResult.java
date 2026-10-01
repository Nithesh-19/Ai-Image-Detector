package com.example.aicontentdetector.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detection_history")
public class DetectionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String result;
    private double confidence;

    public DetectionResult() {
    }

    public DetectionResult(String fileName, String fileType,
                           String result, double confidence) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.result = result;
        this.confidence = confidence;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}