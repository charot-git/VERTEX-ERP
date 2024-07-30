package com.vertex.vos.Utilities;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class ImageCircle {

    public static void circular(ImageView imageView) {
        Circle clip = new Circle();
        clip.setCenterX(imageView.getFitWidth() / 2);
        clip.setCenterY(imageView.getFitHeight() / 2);
        clip.setRadius(Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2);

        imageView.setClip(clip);

        imageView.setPreserveRatio(true);

        imageView.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            clip.setCenterX(newValue.getWidth() / 2);
            clip.setCenterY(newValue.getHeight() / 2);
            clip.setRadius(Math.min(newValue.getWidth(), newValue.getHeight()) / 2);
        });
    }
}
