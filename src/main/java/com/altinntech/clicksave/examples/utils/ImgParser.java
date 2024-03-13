package com.altinntech.clicksave.examples.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImgParser {

    public static byte[] readImageToByteArray(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        return Files.readAllBytes(imageFile.toPath());
    }

    public static BufferedImage byteArrayToImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bis);
    }

    public static void saveImageToFile(BufferedImage image, String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        ImageIO.write(image, "jpg", outputFile);
    }
}
