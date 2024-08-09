package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Module;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private final TilePane tilePane;
    private final Map<String, VBox> moduleVBoxes = new HashMap<>();
    private final ModuleCache moduleCache;

    public ModuleManager(TilePane tilePane, List<VBox> vboxes) {
        this.tilePane = tilePane;
        this.moduleCache = ModuleCache.getInstance();
        initializeModules(vboxes);
    }

    private void initializeModules(List<VBox> vboxes) {
        // Retrieve allowed module IDs for the user
        List<Integer> userAllowedModuleIds = moduleCache.getUserModules();

        // Fetch the allowed modules based on their IDs
        List<Module> userAllowedModules = new ArrayList<>();
        for (int moduleId : userAllowedModuleIds) {
            userAllowedModules.add(moduleCache.getModuleById(moduleId));
        }

        // Iterate over the VBox elements and map them to their module codes
        for (VBox vbox : vboxes) {
            String vboxId = vbox.getId();
            for (Module module : userAllowedModules) {
                String moduleCode = module.getModuleCode();
                if (moduleCode != null && !moduleCode.isEmpty() && moduleCode.equals(vboxId)) {
                    moduleVBoxes.put(moduleCode, vbox);
                    break; // No need to check other modules if we found a match
                }
            }
        }

        // Update the TilePane or other UI components with the new mappings
        updateTilePane();
    }


    public void updateTilePane() {
        tilePane.getChildren().clear();
        for (VBox box : moduleVBoxes.values()) {
            tilePane.getChildren().add(box);
        }
    }

    public void addModule(VBox vbox) {
        String moduleCode = vbox.getId();
        if (moduleCode != null && !moduleCode.isEmpty() && moduleCache.getUserModules().contains(moduleCode)) {
            moduleVBoxes.put(moduleCode, vbox);
            updateTilePane();
        }
    }

    public void removeModule(String moduleCode) {
        moduleVBoxes.remove(moduleCode);
        updateTilePane();
    }
}
