package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Taskbar;
import com.vertex.vos.Objects.UserSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskbarCache {
    private static TaskbarCache instance;
    private final Map<Integer, Taskbar> taskbarCache = new HashMap<>();
    private final TaskbarDAO taskbarDAO = new TaskbarDAO();

    private TaskbarCache() {
        // Load all taskbars into the cache at startup
        List<Taskbar> taskbars = taskbarDAO.getAllTaskbars();
        for (Taskbar taskbar : taskbars) {
            taskbarCache.put(taskbar.getId(), taskbar);
        }
    }

    public static TaskbarCache getInstance() {
        if (instance == null) {
            synchronized (TaskbarCache.class) {
                if (instance == null) {
                    instance = new TaskbarCache();
                }
            }
        }
        return instance;
    }

    public Taskbar getTaskbarById(int taskbarId) {
        return taskbarCache.get(taskbarId);
    }

    public void refreshCache() {
        taskbarCache.clear();
        List<Taskbar> taskbars = taskbarDAO.getAllTaskbars();
        for (Taskbar taskbar : taskbars) {
            taskbarCache.put(taskbar.getId(), taskbar);
        }
    }

    public List<Integer> getUserTaskbars() {
        return taskbarDAO.getTaskbarsForUser(UserSession.getInstance().getUserId());
    }
}
