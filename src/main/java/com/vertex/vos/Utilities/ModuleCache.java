package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Module;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.ModuleDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleCache {
    private static ModuleCache instance;
    private final Map<Integer, Module> moduleCache = new HashMap<>();
    private final ModuleDAO moduleDAO = new ModuleDAO();

    private ModuleCache() {
        // Load all modules into the cache at startup
        List<Module> modules = moduleDAO.getAllModules();
        for (Module module : modules) {
            moduleCache.put(module.getId(), module);
        }
    }

    public static ModuleCache getInstance() {
        if (instance == null) {
            synchronized (ModuleCache.class) {
                if (instance == null) {
                    instance = new ModuleCache();
                }
            }
        }
        return instance;
    }

    public Module getModuleById(int moduleId) {
        return moduleCache.get(moduleId);
    }

    public void refreshCache() {
        moduleCache.clear();
        List<Module> modules = moduleDAO.getAllModules();
        for (Module module : modules) {
            moduleCache.put(module.getId(), module);
        }
    }

    public List<Integer> getUserModules() {
        return moduleDAO.getModulesForUser(UserSession.getInstance().getUserId());
    }
}
