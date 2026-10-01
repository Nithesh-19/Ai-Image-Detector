package com.example.aicontentdetector;

import com.example.aicontentdetector.ai.SmogyModelService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SmogyModelTest {

    public static void main(String[] args) {

        String imagePath =
                "C:\\Users\\Apollo\\Downloads\\Gemini_Generated_Image_3w9exk3w9exk3w9e (4).png";
        SmogyModelService smogy = null;

        try {

            System.out.println("Loading SMOGY...");

            smogy = new SmogyModelService();

            System.out.println("SMOGY loaded.");

            System.out.println();
            System.out.println("Checking image file...");

            File imageFile = new File(imagePath);

            System.out.println("Image path: " + imagePath);
            System.out.println("File exists: " + imageFile.exists());
            System.out.println("File readable: " + imageFile.canRead());
            System.out.println("File size: " + imageFile.length() + " bytes");

            if (!imageFile.exists()) {
                throw new RuntimeException(
                        "Image file does not exist at: " + imagePath
                );
            }

            System.out.println();
            System.out.println("Reading image...");

            BufferedImage image =
                    ImageIO.read(imageFile);

            if (image == null) {
                throw new RuntimeException(
                        "Java found the file, but could not decode it as an image: "
                                + imagePath
                );
            }

            System.out.println(
                    "Image loaded: "
                            + image.getWidth()
                            + " x "
                            + image.getHeight()
            );

            System.out.println();
            System.out.println("Running SMOGY inference...");

            SmogyModelService.SmogyResult result =
                    smogy.predict(image);

            System.out.println();
            System.out.println("==============================");
            System.out.println("       SMOGY RESULT");
            System.out.println("==============================");

            System.out.println(
                    "Prediction: "
                            + result.getLabel()
            );

            System.out.printf(
                    "Artificial: %.2f%%%n",
                    result.getArtificialProbability() * 100
            );

            System.out.printf(
                    "Human: %.2f%%%n",
                    result.getHumanProbability() * 100
            );

            System.out.println("==============================");

        } catch (Exception e) {

            System.out.println();
            System.out.println("SMOGY inference failed.");

            e.printStackTrace();

        } finally {

            if (smogy != null) {
                smogy.close();
            }
        }
    }
}