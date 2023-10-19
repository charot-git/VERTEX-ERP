package com.example.vos;

import javafx.scene.Parent;
import java.util.Stack;

public class NavigationManager {
    private final Stack<Parent> history = new Stack<>();

    public void navigateTo(Parent screen) {
        history.push(screen);
        // Code to set the new screen in your contentPane
    }

    public void navigateBack() {
        if (!history.isEmpty()) {
            history.pop();
            if (!history.isEmpty()) {
                Parent previousScreen = history.peek();
                // Code to set the previous screen in your contentPane
            } else {
                // Handle back navigation when history is empty (e.g., go to home screen)
            }
        } else {
            // Handle back navigation when history is empty (e.g., go to home screen)
        }
    }

    public void navigateForward() {
        // Implement forward navigation if needed
        // This will depend on how you implement forward navigation in your application
    }
}
