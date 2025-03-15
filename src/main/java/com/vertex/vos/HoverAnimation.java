package com.vertex.vos;

import javafx.animation.ScaleTransition;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class HoverAnimation {

    private final Node node;

    public HoverAnimation(Node node) {
        this.node = node;
        node.setCursor(javafx.scene.Cursor.HAND);
        node.toFront();
        setupHoverAnimation();
    }

    private void setupHoverAnimation() {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(100), node);

        scaleIn.setFromX(1);
        scaleIn.setFromY(1);
        scaleIn.setToX(1.1);
        scaleIn.setToY(1.1);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(100), node);
        scaleOut.setFromX(1.1);
        scaleOut.setFromY(1.1);
        scaleOut.setToX(1);
        scaleOut.setToY(1);

        node.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                scaleIn.play();
            }
        });

        node.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                scaleOut.play();
            }
        });
    }
}
