package com.vertex.vos.Utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class TaskbarUtils {
    private static final String TASKBAR_JSON_FILE = "user_taskbars.json";
    private static final TaskbarDAO taskbarDAO  = new TaskbarDAO();



    public static void saveUserTaskbarsToJson(int userId) {
        List<Integer> taskbars = taskbarDAO.getTaskbarsForUser(userId);
        saveTaskbarsToJson(taskbars);
    }

    // Convert list of taskbars to JSON and save to file
    private static void saveTaskbarsToJson(List<Integer> taskbars) {
        Gson gson = new Gson();
        String json = gson.toJson(taskbars);

        try (FileWriter writer = new FileWriter(TASKBAR_JSON_FILE)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load taskbars from JSON file
    public static List<Integer> loadTaskbarsFromJson() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(TASKBAR_JSON_FILE)) {
            Type listType = new TypeToken<List<Integer>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
