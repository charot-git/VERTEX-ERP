package com.vertex.vos.Utilities;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bytedeco.javacv.*;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class JavaFXBarcodeScanner {
    private static volatile boolean scanning = true;
    private static final Queue<String> barcodeQueue = new LinkedList<>();
    private static BufferedImage lastBufferedImage = null;

    private static final Logger logger = Logger.getLogger(JavaFXBarcodeScanner.class.getName());

    static {
        setupLogger();
    }

    private static void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("barcode_scanner.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);

            logger.info("Logger initialized. Logging to barcode_scanner.log");
        } catch (IOException e) {
            System.err.println("⚠ Failed to set up logger: " + e.getMessage());
        }
    }

    public static CompletableFuture<String> startBarcodeScanner(Stage primaryStage) {
        CompletableFuture<String> barcodeFuture = new CompletableFuture<>();
        logger.info("Starting barcode scanner...");

        new Thread(() -> {
            try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
                logger.info("Initializing grabber...");
                grabber.setTimeout(5000);
                grabber.setNumBuffers(1);
                setupGrabber(grabber);

                ImageView imageView = setupImageView();
                setupPrimaryStage(primaryStage, grabber, barcodeFuture, imageView);

                while (scanning) {
                    processFrame(grabber, imageView, barcodeFuture, primaryStage);
                }
                grabber.stop();
                logger.info("Barcode scanner stopped.");
            } catch (FrameGrabber.Exception e) {
                logger.log(Level.SEVERE, "Camera Error: No camera found.", e);
                Platform.runLater(() -> DialogUtils.showErrorMessage("Error", e.getMessage()));
                barcodeFuture.completeExceptionally(new RuntimeException("No camera found."));
            } catch (Exception e) {
                handleException(barcodeFuture, e);
            }
        }).start();

        return barcodeFuture;
    }

    private static void setupGrabber(OpenCVFrameGrabber grabber) throws FrameGrabber.Exception {
        grabber.setImageWidth(320);
        grabber.setImageHeight(240);
        grabber.start();
        logger.info("Camera started successfully.");
    }

    private static ImageView setupImageView() {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(320);
        imageView.setFitHeight(240);
        return imageView;
    }

    private static void setupPrimaryStage(Stage primaryStage, OpenCVFrameGrabber grabber, CompletableFuture<String> barcodeFuture, ImageView imageView) {
        Platform.runLater(() -> {
            Group root = new Group(imageView);
            Scene scene = new Scene(root);
            primaryStage.setTitle("Barcode Scanner");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> handleStageClose(grabber, barcodeFuture));
            primaryStage.show();
        });
    }

    private static void handleStageClose(OpenCVFrameGrabber grabber, CompletableFuture<String> barcodeFuture) {
        scanning = false;
        try {
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            logger.log(Level.SEVERE, "Error stopping grabber", e);
        }
        barcodeFuture.completeExceptionally(new RuntimeException("Stage closed by user"));
    }

    private static void processFrame(OpenCVFrameGrabber grabber, ImageView imageView, CompletableFuture<String> barcodeFuture, Stage primaryStage) throws FrameGrabber.Exception {
        Frame frame = grabber.grab();
        if (frame == null) {
            logger.warning("No frame grabbed from the camera.");
            return;
        }

        BufferedImage bufferedImage = new Java2DFrameConverter().convert(frame);
        if (bufferedImage == null || isSameAsLastFrame(bufferedImage)) {
            return;
        }

        lastBufferedImage = bufferedImage;

        Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
        Platform.runLater(() -> imageView.setImage(fxImage));

        String barcode = scanBarcode(bufferedImage);
        if (barcode != null) {
            handleDetectedBarcode(barcode, barcodeFuture, primaryStage, grabber);
        }
    }

    private static boolean isSameAsLastFrame(BufferedImage currentImage) {
        return lastBufferedImage != null &&
                currentImage.getWidth() == lastBufferedImage.getWidth() &&
                currentImage.getHeight() == lastBufferedImage.getHeight();
    }

    private static void handleDetectedBarcode(String barcode, CompletableFuture<String> barcodeFuture, Stage primaryStage, OpenCVFrameGrabber grabber) {
        logger.info("Barcode Detected: " + barcode);
        barcodeQueue.add(barcode);

        if (barcodeQueue.size() > 3) {
            barcodeQueue.poll();
        }

        if (isConsistentBarcode(barcode)) {
            scanning = false;
            Platform.runLater(primaryStage::close);
            try {
                grabber.stop();
            } catch (FrameGrabber.Exception e) {
                logger.log(Level.SEVERE, "Error stopping grabber", e);
            }
            barcodeFuture.complete(barcode);
        }
    }

    private static void handleException(CompletableFuture<String> barcodeFuture, Exception e) {
        logger.log(Level.SEVERE, "Exception occurred in barcode scanner", e);
        barcodeFuture.completeExceptionally(e);
        Platform.runLater(() -> DialogUtils.showErrorMessage("Error", e.getMessage()));
    }

    private static String scanBarcode(BufferedImage image) {
        if (image == null) return null;
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.CODE_93,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.QR_CODE
            ));

            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        }
    }

    private static boolean isConsistentBarcode(String barcode) {
        return barcodeQueue.size() >= 3 && barcodeQueue.stream().allMatch(b -> b.equals(barcode));
    }
}
