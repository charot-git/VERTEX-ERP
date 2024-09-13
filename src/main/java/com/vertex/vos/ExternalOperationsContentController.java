package com.vertex.vos;

import com.vertex.vos.Utilities.HistoryManager;
import javafx.scene.layout.AnchorPane;

public class ExternalOperationsContentController {
    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

}
