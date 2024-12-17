package com.vertex.vos.Utilities;

import com.google.gson.Gson;
import java.io.*;
import java.util.Map;

public class LocationCache {
    private static final Gson gson = new Gson();
    private static Map<String, String> provinceData;
    private static Map<String, String> cityData;
    private static Map<String, String> barangayData;

    static {
        initialize();
    }

    public static Map<String, String> getProvinceData() {
        return provinceData;
    }

    public static Map<String, String> getCityData() {
        return cityData;
    }

    public static Map<String, String> getBarangayData() {
        return barangayData;
    }

    public static void initialize() {
        try {
            provinceData = readDataFromResource("provinceData.json");
            cityData = readDataFromResource("cityData.json");
            barangayData = readDataFromResource("barangayData.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> readDataFromResource(String fileName) throws IOException {
        try (InputStream inputStream = LocationCache.class.getClassLoader().getResourceAsStream(fileName);
             Reader reader = new InputStreamReader(inputStream)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + fileName);
            }
            return gson.fromJson(reader, Map.class);
        }
    }
}
