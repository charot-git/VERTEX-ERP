package com.vertex.vos;

import com.vertex.vos.Utilities.DialogUtils;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class ContentManager {
    public static void setContent(AnchorPane contentPane, Parent content) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(content);

        double maxNavigatorWidth = contentPane.getWidth() - 20; // Adjust as needed
        double maxNavigatorHeight = contentPane.getHeight() - 20; // Adjust as needed
        contentPane.setMaxSize(maxNavigatorWidth, maxNavigatorHeight);
        contentPane.setMinSize(0, 0); // Allow contentPane to shrink smaller if necessary

        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
    }

}
