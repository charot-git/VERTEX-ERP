package com.vertex.vos;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import javafx.scene.image.WritableImage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

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
            Graphics2D g2d = awtImage.createGraphics();

            // Set background color (optional)
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, imageWidth, imageHeight);

            // Draw the barcode with margins
            g2d.translate(marginX, marginY);  // Shift origin for barcode placement
            barcode.paint(g2d);
            g2d.dispose();

            // Convert the BufferedImage to a WritableImage (black and white)
            WritableImage fxImage = new WritableImage(imageWidth, imageHeight);

            // Convert the BufferedImage to WritableImage
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
                    fxImage.getPixelWriter().setArgb(x, y, awtImage.getRGB(x, y));
                }
            }

            return fxImage;
        } catch (Exception e) {
            System.err.println("Error generating barcode image: " + e.getMessage());
            return null;
        }
    }
}
