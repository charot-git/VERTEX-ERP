package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Taskbar;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskbarManager {
    private final VBox parentVBox; // Parent container for taskbars
    private final Map<String, VBox> taskbarVBoxes = new HashMap<>();
    private final TaskbarCache taskbarCache;

    public TaskbarManager(VBox parentVBox, List<VBox> vboxes) {
        this.parentVBox = parentVBox;
        this.taskbarCache = TaskbarCache.getInstance();
        initializeTaskbars(vboxes);
    }

    private void initializeTaskbars(List<VBox> vboxes) {
        // Retrieve allowed taskbar IDs for the user
        List<Integer> userAllowedTaskbarIds = taskbarCache.getUserTaskbars();

        // Fetch the allowed taskbars based on their IDs
        List<Taskbar> userAllowedTaskbars = new ArrayList<>();
        for (int taskbarId : userAllowedTaskbarIds) {
            userAllowedTaskbars.add(taskbarCache.getTaskbarById(taskbarId));
        }

        // Iterate over the VBox elements and map them to their taskbar codes
        for (VBox vbox : vboxes) {
            String vboxId = vbox.getId();
            for (Taskbar taskbar : userAllowedTaskbars) {
                String taskbarCode = taskbar.getTaskbarCode();
                if (taskbarCode != null && !taskbarCode.isEmpty() && taskbarCode.equals(vboxId)) {
                    taskbarVBoxes.put(taskbarCode, vbox);
                    break; // No need to check other taskbars if we found a match
                }
            }
        }

        // Update the parent VBox or other UI components with the new mappings
        updateParentVBox();
    }

    public void updateParentVBox() {
        parentVBox.getChildren().clear();
        for (VBox box : taskbarVBoxes.values()) {
            parentVBox.getChildren().add(box);
        }
    }

    public void addTaskbar(VBox vbox) {
        String taskbarCode = vbox.getId();
        if (taskbarCode != null && !taskbarCode.isEmpty() && taskbarCache.getUserTaskbars().contains(taskbarCode)) {
            taskbarVBoxes.put(taskbarCode, vbox);
            updateParentVBox();
        }
    }

    public void removeTaskbar(String taskbarCode) {
        taskbarVBoxes.remove(taskbarCode);
        updateParentVBox();
    }
}
