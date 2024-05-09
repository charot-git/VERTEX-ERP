package com.vertex.vos;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.awt.Color;

public class BarcodePrinter {

    // Margins for the image (adjust as needed)
    private static final int marginX = 10;
    private static final int marginY = 5;

    // Method to generate black and white barcode image with margins
    public static WritableImage generateBarcodeImage(String barcodeText) {
        try {
            // Create a Barcode object (choose appropriate type for your scanner)
            Barcode barcode = BarcodeFactory.createCode128(barcodeText);

            // Calculate total image width and height including margins
            int imageWidth = barcode.getWidth() + (2 * marginX);
            int imageHeight = barcode.getHeight() + (2 * marginY);

            // Create a BufferedImage with margins
            BufferedImage awtImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

            // Draw the barcode with margins
            java.awt.Graphics2D g2d = awtImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, imageWidth, imageHeight);
            g2d.translate(marginX, marginY);  // Shift origin for barcode placement
            barcode.paint(g2d);
            g2d.dispose();

            // Convert BufferedImage to WritableImage (PNG format)
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
