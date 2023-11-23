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
        if (isDataAvailableLocally()) {
            try {
                provinceData = readDataFromFile("provinceData.json");
                cityData = readDataFromFile("cityData.json");
                barangayData = readDataFromFile("barangayData.json");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                provinceData = LocationUtils.fetchLocationData("https://ph-locations-api.buonzz.com/v1/provinces");
                cityData = LocationUtils.fetchLocationData("https://ph-locations-api.buonzz.com/v1/cities");
                barangayData = LocationUtils.fetchLocationData("https://ph-locations-api.buonzz.com/v1/barangays");

                // Store the data locally as JSON
                storeDataLocally("provinceData.json", provinceData);
                storeDataLocally("cityData.json", cityData);
                storeDataLocally("barangayData.json", barangayData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isDataAvailableLocally() {
        File provinceFile = new File("provinceData.json");
        File cityFile = new File("cityData.json");
        File barangayFile = new File("barangayData.json");
        return provinceFile.exists() && cityFile.exists() && barangayFile.exists();
    }

    private static void storeDataLocally(String fileName, Map<String, String> data) throws IOException {
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(data, writer);
        }
    }

    private static Map<String, String> readDataFromFile(String fileName) throws IOException {
        try (Reader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, Map.class);
        }
    }
}
