package com.vertex.vos.Utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ModuleUtils {
    private static final String MODULE_JSON_FILE = "user_modules.json";
    private static final ModuleDAO moduleDAO = new ModuleDAO();

    // Save user modules to JSON file
    public static void saveUserModulesToJson(int userId) {
        List<Integer> modules = moduleDAO.getModulesForUser(userId);
        saveModulesToJson(modules);
    }

    // Convert list of modules to JSON and save to file
    private static void saveModulesToJson(List<Integer> modules) {
        Gson gson = new Gson();
        String json = gson.toJson(modules);

        try (FileWriter writer = new FileWriter(MODULE_JSON_FILE)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load modules from JSON file
    public static List<Integer> loadModulesFromJson() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(MODULE_JSON_FILE)) {
            Type listType = new TypeToken<List<Integer>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
