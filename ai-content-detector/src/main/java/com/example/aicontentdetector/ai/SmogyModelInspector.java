package com.example.aicontentdetector.ai;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class SmogyModelInspector {

    public static void main(String[] args) throws Exception {

        OrtEnvironment environment =
                OrtEnvironment.getEnvironment();

        OrtSession.SessionOptions options =
                new OrtSession.SessionOptions();

        OrtSession session =
                environment.createSession(
                        "src/main/resources/models/model-smogy.onnx",
                        options
                );

        System.out.println("========== SMOGY MODEL INFO ==========");

        System.out.println("Input names:");
        for (String name : session.getInputNames()) {
            System.out.println("  " + name);

            System.out.println(
                    "  Info: "
                            + session.getInputInfo().get(name)
            );
        }

        System.out.println("Output names:");
        for (String name : session.getOutputNames()) {
            System.out.println("  " + name);

            System.out.println(
                    "  Info: "
                            + session.getOutputInfo().get(name)
            );
        }

        System.out.println("======================================");

        session.close();
        environment.close();
    }
}