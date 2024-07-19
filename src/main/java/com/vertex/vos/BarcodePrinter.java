package com.vertex.vos;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BarcodePrinter {

    private static final int MARGIN_X = 20; // Adjusted margins for better fitting
    private static final int MARGIN_Y = 10;

    /**
     * Generates a barcode image with margins.
     *
     * @param barcodeText the text to encode in the barcode
     * @return a WritableImage containing the barcode
     */
    public static WritableImage generateBarcodeEAN(String barcodeText) {
        try {
            // Create the Barcode object
            Barcode barcode = BarcodeFactory.createEAN128(barcodeText);

            barcode.setFont(new Font("Times New Roman", Font.PLAIN, 16));
            barcode.setBarWidth(2); // Adjusted for better visibility
            barcode.setBarHeight(60); // Adjusted for better fitting
            barcode.setBackground(Color.WHITE);

            // Calculate dimensions including margins
            int barcodeWidth = barcode.getWidth();
            int barcodeHeight = barcode.getHeight();
            int imageWidth = barcodeWidth + (2 * MARGIN_X);
            int imageHeight = barcodeHeight + (2 * MARGIN_Y);

            // Create a BufferedImage
            BufferedImage awtImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = awtImage.createGraphics();

            try {
                // Fill background
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, imageWidth, imageHeight);

                // Draw barcode with margins
                g2d.setColor(Color.BLACK);
                g2d.translate(MARGIN_X, MARGIN_Y); // Shift origin for barcode placement
                barcode.paint(g2d);
            } finally {
                g2d.dispose(); // Ensure resources are cleaned up
            }

            // Convert BufferedImage to WritableImage
            WritableImage fxImage = new WritableImage(imageWidth, imageHeight);
            javafx.scene.image.PixelWriter pixelWriter = fxImage.getPixelWriter();
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
                    int rgba = awtImage.getRGB(x, y);
                    pixelWriter.setArgb(x, y, rgba);
                }
            }

            return fxImage;
        } catch (Exception e) {
            System.err.println("Error generating barcode image: " + e.getMessage());
            return null;
        }
    }

    public static WritableImage generateBarcodeCode128(String barcodeText) {
        try {
            // Create the Barcode object
            Barcode barcode = BarcodeFactory.createCode128(barcodeText);

            barcode.setFont(new Font("Times New Roman", Font.PLAIN, 16));
            barcode.setBarWidth(2); // Adjusted for better visibility
            barcode.setBarHeight(60); // Adjusted for better fitting
            barcode.setBackground(Color.WHITE);

            // Calculate dimensions including margins
            int barcodeWidth = barcode.getWidth();
            int barcodeHeight = barcode.getHeight();
            int imageWidth = barcodeWidth + (2 * MARGIN_X);
            int imageHeight = barcodeHeight + (2 * MARGIN_Y);

            // Create a BufferedImage
            BufferedImage awtImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = awtImage.createGraphics();

            try {
                // Fill background
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, imageWidth, imageHeight);

                // Draw barcode with margins
                g2d.setColor(Color.BLACK);
                g2d.translate(MARGIN_X, MARGIN_Y); // Shift origin for barcode placement
                barcode.paint(g2d);
            } finally {
                g2d.dispose(); // Ensure resources are cleaned up
            }

            // Convert BufferedImage to WritableImage
            WritableImage fxImage = new WritableImage(imageWidth, imageHeight);
            javafx.scene.image.PixelWriter pixelWriter = fxImage.getPixelWriter();
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
                    int rgba = awtImage.getRGB(x, y);
                    pixelWriter.setArgb(x, y, rgba);
                }
            }

            return fxImage;
        } catch (Exception e) {
            System.err.println("Error generating barcode image: " + e.getMessage());
            return null;
        }
    }
}
