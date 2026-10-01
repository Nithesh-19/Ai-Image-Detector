package com.example.aicontentdetector.service;

import com.example.aicontentdetector.ai.AiModelService;
import com.example.aicontentdetector.ai.AiModelService.AiPrediction;
import com.example.aicontentdetector.model.DetectionResult;
import com.example.aicontentdetector.repository.DetectionRepository;
import java.util.List;
import nu.pattern.OpenCV;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DetectionService {

    // Load OpenCV native library
    static {
        OpenCV.loadLocally();
    }

    private final DetectionRepository detectionRepository;
    private final AiModelService aiModelService;

    // Constructor
    public DetectionService(
            DetectionRepository detectionRepository,
            AiModelService aiModelService) {

        this.detectionRepository = detectionRepository;
        this.aiModelService = aiModelService;
    }

    // ----------------------------------------------------
    // OLD TEST METHOD
    // ----------------------------------------------------

    public DetectionResult detect() {

        DetectionResult result = new DetectionResult(
                "test-image.jpg",
                "IMAGE",
                "AI_GENERATED",
                95.5
        );

        return detectionRepository.save(result);
    }

    // ----------------------------------------------------
    // FILE VALIDATION
    // ----------------------------------------------------

    public String validateFile(MultipartFile file) {

        // Check whether file exists
        if (file == null || file.isEmpty()) {

            return "File is empty";
        }

        // Get original file name
        String fileName =
                file.getOriginalFilename();

        // Check file name
        if (fileName == null) {

            return "Invalid file name";
        }

        // Maximum file size = 50 MB
        long maxFileSize =
                50 * 1024 * 1024;

        if (file.getSize() > maxFileSize) {

            return "File is too large. Maximum allowed size is 50 MB";
        }

        // Get MIME type
        String contentType =
                file.getContentType();

        if (contentType == null) {

            return "Unknown file type";
        }

        // Convert to lowercase
        String lowerCaseFileName =
                fileName.toLowerCase();

        String lowerCaseContentType =
                contentType.toLowerCase();

        // ------------------------------------------------
        // JPG / JPEG
        // ------------------------------------------------

        if ((lowerCaseFileName.endsWith(".jpg")
                || lowerCaseFileName.endsWith(".jpeg"))
                && lowerCaseContentType.equals("image/jpeg")) {

            return "Valid IMAGE file: " + fileName;
        }

        // ------------------------------------------------
        // PNG
        // ------------------------------------------------

        if (lowerCaseFileName.endsWith(".png")
                && lowerCaseContentType.equals("image/png")) {

            return "Valid IMAGE file: " + fileName;
        }

        // ------------------------------------------------
        // MP4
        // ------------------------------------------------

        if (lowerCaseFileName.endsWith(".mp4")
                && lowerCaseContentType.equals("video/mp4")) {

            return "Valid VIDEO file: " + fileName;
        }

        // ------------------------------------------------
        // AVI
        // ------------------------------------------------

        if (lowerCaseFileName.endsWith(".avi")
                && lowerCaseContentType.equals("video/x-msvideo")) {

            return "Valid VIDEO file: " + fileName;
        }

        // ------------------------------------------------
        // MOV
        // ------------------------------------------------

        if (lowerCaseFileName.endsWith(".mov")
                && lowerCaseContentType.equals("video/quicktime")) {

            return "Valid VIDEO file: " + fileName;
        }

        // ------------------------------------------------
        // Unsupported file
        // ------------------------------------------------

        return "Unsupported or mismatched file type";
    }

    // ----------------------------------------------------
    // IMAGE DETECTION
    // ----------------------------------------------------

    public DetectionResult readImage(
            MultipartFile file) {


        try {

            // Step 1:
            // Validate uploaded file

            String validationResult =
                    validateFile(file);

            if (!validationResult.startsWith(
                    "Valid IMAGE")) {

                throw new IllegalArgumentException(
                        validationResult
                );
            }

            // Step 2:
            // Convert uploaded file into bytes

            byte[] imageBytes =
                    file.getBytes();

            // Step 3:
            // Convert bytes into OpenCV Mat

            MatOfByte matOfByte =
                    new MatOfByte(imageBytes);

            // Step 4:
            // Decode image

            Mat image =
                    Imgcodecs.imdecode(
                            matOfByte,
                            Imgcodecs.IMREAD_COLOR
                    );

            // We no longer need the byte container
            matOfByte.release();

            // Step 5:
            // Check whether OpenCV successfully
            // decoded the image

            if (image.empty()) {

                image.release();

                throw new IllegalArgumentException(
                        "Unable to read image"
                );
            }

            try {

                // Step 6:
                // Send OpenCV image to AI model

                AiPrediction prediction =
                        aiModelService.predict(image);

                // Step 7:
                // Create DetectionResult object

                DetectionResult detectionResult =
                        new DetectionResult(
                                file.getOriginalFilename(),
                                "IMAGE",
                                prediction.getResult(),
                                prediction.getConfidence()
                        );

                // Step 8:
                // Save result in MySQL

                return detectionRepository.save(
                        detectionResult
                );

            }
            finally {

                // Step 9:
                // Release OpenCV memory

                image.release();
            }

        } catch (IllegalArgumentException e) {

            // File validation / image error
            throw e;

        } catch (Exception e) {

            // Any other error
            e.printStackTrace();

            throw new RuntimeException(
                    "Prediction error: "
                            + e.getMessage()
            );
        }
    }public List<DetectionResult> getHistory() {

        return detectionRepository.findAll();
    }
}