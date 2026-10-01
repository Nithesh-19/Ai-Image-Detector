package com.example.aicontentdetector.controller;

import com.example.aicontentdetector.ai.SmogyModelService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
@RestController
@RequestMapping("/api/smogy")
@CrossOrigin(origins = "http://localhost:5173")
public class SmogyController {


    private final SmogyModelService smogyModelService;

    public SmogyController(SmogyModelService smogyModelService) {
        this.smogyModelService = smogyModelService;
    }

    @PostMapping(
            value = "/detect",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public SmogyResponse detect(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        BufferedImage image =
                ImageIO.read(file.getInputStream());

        if (image == null) {
            throw new IllegalArgumentException(
                    "Uploaded file is not a valid image."
            );
        }

        SmogyModelService.SmogyResult result =
                smogyModelService.predict(image);

        return new SmogyResponse(
                file.getOriginalFilename(),
                "IMAGE",
                result.getLabel(),
                result.getArtificialProbability() * 100,
                result.getHumanProbability() * 100
        );
    }

    public static class SmogyResponse {

        private final String fileName;
        private final String fileType;
        private final String prediction;
        private final double artificialScore;
        private final double humanScore;

        public SmogyResponse(
                String fileName,
                String fileType,
                String prediction,
                double artificialScore,
                double humanScore
        ) {
            this.fileName = fileName;
            this.fileType = fileType;
            this.prediction = prediction;
            this.artificialScore = artificialScore;
            this.humanScore = humanScore;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileType() {
            return fileType;
        }

        public String getPrediction() {
            return prediction;
        }

        public double getArtificialScore() {
            return artificialScore;
        }

        public double getHumanScore() {
            return humanScore;
        }
    }
}