package com.example.aicontentdetector.ai;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import org.springframework.stereotype.Service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

@Service
public class SmogyModelService {

    private static final int IMAGE_SIZE = 224;

    private static final float[] MEAN = {
            0.485f,
            0.456f,
            0.406f
    };

    private static final float[] STD = {
            0.229f,
            0.224f,
            0.225f
    };

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final String inputName;


    public SmogyModelService() throws Exception {

        // Use the same ONNX Runtime environment
        // that DETECTRA uses.
        environment = OrtEnvironment.getEnvironment();

        OrtSession.SessionOptions options =
                new OrtSession.SessionOptions();


        // ------------------------------------------------
        // Load SMOGY model from src/main/resources
        // ------------------------------------------------

        InputStream modelStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                "models/model-smogy.onnx"
                        );

        if (modelStream == null) {

            throw new IOException(
                    "SMOGY model not found: "
                            + "src/main/resources/models/model-smogy.onnx"
            );
        }


        // ------------------------------------------------
        // Copy resource to temporary ONNX file
        // ------------------------------------------------

        Path temporaryModel =
                Files.createTempFile(
                        "smogy-",
                        ".onnx"
                );

        try (InputStream input = modelStream) {

            Files.copy(
                    input,
                    temporaryModel,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }


        // ------------------------------------------------
        // Create ONNX Runtime session
        // ------------------------------------------------

        session =
                environment.createSession(
                        temporaryModel.toString(),
                        options
                );


        // Get model input name
        inputName =
                session.getInputNames()
                        .iterator()
                        .next();


        // ------------------------------------------------
        // Print model information
        // ------------------------------------------------

        System.out.println();
        System.out.println("========== SMOGY MODEL INFO ==========");

        System.out.println("Input names:");

        for (String name : session.getInputNames()) {

            System.out.println(
                    "  " + name
            );
        }

        System.out.println("Output names:");

        for (String name : session.getOutputNames()) {

            System.out.println(
                    "  " + name
            );
        }

        System.out.println("======================================");

        System.out.println(
                "================================="
        );

        System.out.println(
                "SMOGY ONNX MODEL LOADED SUCCESSFULLY"
        );

        System.out.println(
                "Input name: " + inputName
        );

        System.out.println(
                "================================="
        );
    }


    // ====================================================
    // PREDICTION
    // ====================================================

    public SmogyResult predict(
            BufferedImage originalImage
    ) throws OrtException {


        if (originalImage == null) {

            throw new IllegalArgumentException(
                    "Image cannot be null."
            );
        }


        // ------------------------------------------------
        // Step 1: Resize image to 224 x 224
        // ------------------------------------------------

        BufferedImage resizedImage =
                new BufferedImage(
                        IMAGE_SIZE,
                        IMAGE_SIZE,
                        BufferedImage.TYPE_INT_RGB
                );


        Graphics2D graphics =
                resizedImage.createGraphics();


        graphics.drawImage(
                originalImage,
                0,
                0,
                IMAGE_SIZE,
                IMAGE_SIZE,
                null
        );


        graphics.dispose();


        // ------------------------------------------------
        // Step 2: Create CHW float array
        // ------------------------------------------------

        int area =
                IMAGE_SIZE * IMAGE_SIZE;


        float[] chw =
                new float[3 * area];


        // ------------------------------------------------
        // Step 3: Convert RGB pixels
        //         0-255 -> 0-1
        //         ImageNet normalization
        // ------------------------------------------------

        for (int y = 0; y < IMAGE_SIZE; y++) {

            for (int x = 0; x < IMAGE_SIZE; x++) {

                int rgb =
                        resizedImage.getRGB(
                                x,
                                y
                        );


                int red =
                        (rgb >> 16) & 0xFF;

                int green =
                        (rgb >> 8) & 0xFF;

                int blue =
                        rgb & 0xFF;


                // Convert 0-255 -> 0-1

                float redValue =
                        red / 255.0f;

                float greenValue =
                        green / 255.0f;

                float blueValue =
                        blue / 255.0f;


                int index =
                        y * IMAGE_SIZE + x;


                // ImageNet normalization

                chw[index] =
                        (redValue - MEAN[0])
                                / STD[0];


                chw[area + index] =
                        (greenValue - MEAN[1])
                                / STD[1];


                chw[2 * area + index] =
                        (blueValue - MEAN[2])
                                / STD[2];
            }
        }


        // ------------------------------------------------
        // Step 4: Create ONNX tensor
        // Shape:
        //
        // [1, 3, 224, 224]
        // ------------------------------------------------

        long[] shape = {
                1,
                3,
                IMAGE_SIZE,
                IMAGE_SIZE
        };


        try (OnnxTensor inputTensor =
                     OnnxTensor.createTensor(
                             environment,
                             FloatBuffer.wrap(chw),
                             shape
                     );

             OrtSession.Result output =
                     session.run(
                             Collections.singletonMap(
                                     inputName,
                                     inputTensor
                             )
                     )) {


            // ------------------------------------------------
            // Step 5: Read model output
            // ------------------------------------------------

            Object value =
                    output.get(0).getValue();


            float[] logits;


            // Some ONNX models return float[][]
            // Example: [[2.5, -1.2]]

            if (value instanceof float[][]) {

                float[][] outputArray =
                        (float[][]) value;


                if (outputArray.length == 0 ||
                        outputArray[0].length < 2) {

                    throw new IllegalStateException(
                            "SMOGY returned invalid output."
                    );
                }


                logits =
                        outputArray[0];

            }

            // Some models may return float[]
            // Example: [2.5, -1.2]

            else if (value instanceof float[]) {

                logits =
                        (float[]) value;

            }

            else {

                throw new IllegalStateException(
                        "Unexpected SMOGY output type: "
                                + value.getClass()
                );
            }


            if (logits.length < 2) {

                throw new IllegalStateException(
                        "SMOGY returned fewer than 2 logits."
                );
            }


            // ------------------------------------------------
            // Step 6: Get the two logits
            //
            // SMOGY:
            //
            // logits[0] = ARTIFICIAL
            // logits[1] = HUMAN
            // ------------------------------------------------

            double artificialLogit =
                    logits[0];

            double humanLogit =
                    logits[1];


            // ------------------------------------------------
            // Step 7: Softmax
            // ------------------------------------------------

            double maxLogit =
                    Math.max(
                            artificialLogit,
                            humanLogit
                    );


            double artificialExp =
                    Math.exp(
                            artificialLogit
                                    - maxLogit
                    );


            double humanExp =
                    Math.exp(
                            humanLogit
                                    - maxLogit
                    );


            double sum =
                    artificialExp + humanExp;


            double artificialProbability =
                    artificialExp / sum;


            double humanProbability =
                    humanExp / sum;


            // ------------------------------------------------
            // Step 8: Determine prediction
            // ------------------------------------------------

            String prediction;


            if (artificialProbability >
                    humanProbability) {

                prediction = "ARTIFICIAL";

            } else {

                prediction = "HUMAN";
            }


            // ------------------------------------------------
            // Print result
            // ------------------------------------------------

            System.out.println();
            System.out.println(
                    "========== SMOGY OUTPUT =========="
            );

            System.out.println(
                    "Artificial logit: "
                            + artificialLogit
            );

            System.out.println(
                    "Human logit: "
                            + humanLogit
            );

            System.out.println(
                    "Artificial probability: "
                            + artificialProbability
            );

            System.out.println(
                    "Human probability: "
                            + humanProbability
            );

            System.out.println(
                    "Artificial (%): "
                            + (artificialProbability * 100)
            );

            System.out.println(
                    "Human (%): "
                            + (humanProbability * 100)
            );

            System.out.println(
                    "Prediction: "
                            + prediction
            );

            System.out.println(
                    "=================================="
            );


            // ------------------------------------------------
            // Step 9: Return result
            // ------------------------------------------------

            return new SmogyResult(
                    prediction,
                    (float) artificialProbability,
                    (float) humanProbability
            );
        }
    }


    // ====================================================
    // RESULT CLASS
    // ====================================================

    public static class SmogyResult {

        private final String label;

        private final float artificialProbability;

        private final float humanProbability;


        public SmogyResult(
                String label,
                float artificialProbability,
                float humanProbability
        ) {

            this.label = label;

            this.artificialProbability =
                    artificialProbability;

            this.humanProbability =
                    humanProbability;
        }


        public String getLabel() {

            return label;
        }


        public float getArtificialProbability() {

            return artificialProbability;
        }


        public float getHumanProbability() {

            return humanProbability;
        }


        @Override
        public String toString() {

            return String.format(
                    "SMOGY Result: %s | Artificial: %.2f%% | Human: %.2f%%",
                    label,
                    artificialProbability * 100,
                    humanProbability * 100
            );
        }
    }


    // ====================================================
    // CLOSE SESSION
    // ====================================================

    public void close() {

        if (session != null) {

            try {

                session.close();

            } catch (OrtException e) {

                e.printStackTrace();
            }
        }
    }
}