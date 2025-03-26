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

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class JavaFXBarcodeScanner {
    private static volatile boolean scanning = true;
    private static final Queue<String> barcodeQueue = new LinkedList<>();

    public static CompletableFuture<String> startBarcodeScanner(Stage primaryStage) {
        CompletableFuture<String> barcodeFuture = new CompletableFuture<>();

        new Thread(() -> {
            try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
                grabber.setImageWidth(640);
                grabber.setImageHeight(480);
                grabber.start();

                ImageView imageView = new ImageView();
                imageView.setFitWidth(640);
                imageView.setFitHeight(480);

                Platform.runLater(() -> {
                    Group root = new Group(imageView);
                    Scene scene = new Scene(root);
                    primaryStage.setTitle("Barcode Scanner");
                    primaryStage.setScene(scene);
                    primaryStage.show();
                });

                while (scanning) {
                    Frame frame = grabber.grab();
                    if (frame == null) {
                        System.out.println("⚠ No frame captured.");
                        continue;
                    }

                    BufferedImage bufferedImage = new Java2DFrameConverter().convert(frame);
                    if (bufferedImage == null) {
                        System.out.println("⚠ Frame conversion failed.");
                        continue;
                    }

                    Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    Platform.runLater(() -> imageView.setImage(fxImage));

                    String barcode = scanBarcode(bufferedImage);

                    if (barcode != null) {
                        System.out.println("🔍 Barcode Detected: " + barcode);
                        barcodeQueue.add(barcode);

                        if (barcodeQueue.size() > 3) {
                            barcodeQueue.poll();
                        }

                        if (isConsistentBarcode(barcode)) {
                            System.out.println("✅ Verified Barcode: " + barcode);
                            scanning = false;
                            Platform.runLater(primaryStage::close);
                            grabber.stop();
                            barcodeFuture.complete(barcode);
                            return;
                        }
                    }
                    Thread.sleep(200);
                }
                grabber.stop();
            } catch (Exception e) {
                barcodeFuture.completeExceptionally(e);
                e.printStackTrace();
            }
        }).start();

        return barcodeFuture;
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
            System.out.println("❌ No barcode detected.");
            return null;
        }
    }

    private static boolean isConsistentBarcode(String barcode) {
        if (barcodeQueue.size() < 3) return false;
        return barcodeQueue.stream().allMatch(b -> b.equals(barcode));
    }
}
