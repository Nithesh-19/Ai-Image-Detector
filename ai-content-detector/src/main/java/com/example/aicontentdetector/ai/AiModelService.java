package com.example.aicontentdetector.ai;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import org.springframework.stereotype.Service;

import java.nio.FloatBuffer;
import java.util.Collections;

@Service
public class AiModelService {

    private static final int MODEL_SIZE = 384;
    private static final int RESIZE_SIZE = 440;

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final String inputName;

    public AiModelService() throws OrtException {

        environment = OrtEnvironment.getEnvironment();

        OrtSession.SessionOptions options =
                new OrtSession.SessionOptions();

        session = environment.createSession(
                "src/main/resources/models/model-detectra.onnx",
                options
        );

        inputName = session.getInputNames()
                .iterator()
                .next();
        System.out.println("========== DETECTRA MODEL INFO ==========");

        System.out.println("Input names:");
        for (String name : session.getInputNames()) {
            System.out.println("  " + name);
        }

        System.out.println("Output names:");
        for (String name : session.getOutputNames()) {
            System.out.println("  " + name);
        }

        System.out.println("==========================================");

        System.out.println("=================================");
        System.out.println("ONNX MODEL LOADED SUCCESSFULLY");
        System.out.println("Input name: " + inputName);
        System.out.println("=================================");
    }

    public AiPrediction predict(Mat originalImage)
            throws OrtException {

        Mat resized = new Mat();
        Mat cropped = new Mat();
        Mat rgb = new Mat();
        Mat floatImage = new Mat();

        try {

            // Step 1: Resize image to 224 × 224
            int originalWidth = originalImage.cols();
            int originalHeight = originalImage.rows();

            double scale = (double) RESIZE_SIZE
                    / Math.min(originalWidth, originalHeight);

            int newWidth = (int) Math.round(originalWidth * scale);
            int newHeight = (int) Math.round(originalHeight * scale);

            Imgproc.resize(
                    originalImage,
                    resized,
                    new Size(newWidth, newHeight),
                    0,
                    0,
                    Imgproc.INTER_LINEAR
            );
            int cropX = (resized.cols() - MODEL_SIZE) / 2;
            int cropY = (resized.rows() - MODEL_SIZE) / 2;

             cropped = new Mat(
                    resized,
                    new Rect(
                            cropX,
                            cropY,
                            MODEL_SIZE,
                            MODEL_SIZE
                    )
            );
            // Step 2: Convert BGR → RGB
            Imgproc.cvtColor(
                    cropped,
                    rgb,
                    Imgproc.COLOR_BGR2RGB
            );

            // Step 3: Convert 8-bit image to float
            // and scale 0-255 → 0-1
            rgb.convertTo(
                    floatImage,
                    CvType.CV_32FC3,
                    1.0 / 255.0
            );

            // Step 4: Create CHW array
            float[] chw =
                    new float[3 * MODEL_SIZE * MODEL_SIZE];

            int area = MODEL_SIZE * MODEL_SIZE;

            for (int y = 0; y < MODEL_SIZE; y++) {

                for (int x = 0; x < MODEL_SIZE; x++) {

                    double[] pixel =
                            floatImage.get(y, x);

                    int index =
                            y * MODEL_SIZE + x;

                    // RGB channel 0
                    chw[index] =
                            (float) ((pixel[0] - 0.485) / 0.229);

                    chw[area + index] =
                            (float) ((pixel[1] - 0.456) / 0.224);

                    chw[2 * area + index] =
                            (float) ((pixel[2] - 0.406) / 0.225);
                }
            }

            // Step 5: Create ONNX input tensor
            long[] shape = {
                    1,
                    3,
                    MODEL_SIZE,
                    MODEL_SIZE
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

                // Step 6: Get model output
                Object value =
                        output.get(0).getValue();

                if (!(value instanceof float[][])) {

                    throw new IllegalStateException(
                            "Unexpected model output type: "
                                    + value.getClass()
                    );
                }

                float[][] outputArray =
                        (float[][]) value;

                float logit =
                        outputArray[0][0];

                double aiProbability =
                        1.0 / (1.0 + Math.exp(-logit));

                System.out.println("========== MODEL OUTPUT ==========");
                System.out.println("AI logit: " + logit);
                System.out.println("AI probability: " + aiProbability);
                System.out.println("AI probability (%): "
                        + (aiProbability * 100));
                System.out.println("==================================");

                String result;

                if (aiProbability >= 0.65) {
                    result = "AI_GENERATED";
                } else {
                    result = "REAL";
                }

                double confidence;

                if (aiProbability >= 0.65) {
                    confidence = aiProbability * 100;
                } else {
                    confidence = (1.0 - aiProbability) * 100;
                }

                return new AiPrediction(
                        result,
                        confidence
                );
            }
            } finally {

                resized.release();
                cropped.release();
                rgb.release();
                floatImage.release();
            }}


            public static class AiPrediction {

                private final String result;
                private final double confidence;

                public AiPrediction(
                        String result,
                        double confidence) {

                    this.result = result;
                    this.confidence = confidence;
                }

                public String getResult() {
                    return result;
                }

                public double getConfidence() {
                    return confidence;
                }
            }
        }