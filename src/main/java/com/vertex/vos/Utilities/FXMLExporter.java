package com.vertex.vos.Utilities;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FXMLExporter {

    public static void exportToImage(Node containerNode, String fileName, Stage stage) throws IOException {
        WritableImage snapshot = containerNode.snapshot(new SnapshotParameters(), null);
        exportImage(snapshot, stage, fileName);
    }

    private static void exportImage(WritableImage image, Stage stage, String defaultFileName) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Save Image (PNG)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        fileChooser.setInitialFileName(defaultFileName + ".png");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                filePath += ".png";
                file = new File(filePath);
            }

            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            PixelReader pixelReader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = pixelReader.getArgb(x, y);
                    bufferedImage.setRGB(x, y, argb);
                }
            }

            try {
                ImageIO.write(bufferedImage, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception as needed
            }
        }
    }
}
