package com.vertex.vos.Utilities;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LocationCache {
    private static Map<String, String> provinceData; // Map to store province codes and names
    private static Map<String, String> cityData; // Map to store city codes and names
    private static Map<String, String> barangayData; // Map to store barangay codes and names

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
            provinceData = LocationUtils.fetchLocationData("https://ph-locations-api.buonzz.com/v1/provinces");
            cityData = LocationUtils.fetchLocationData("https://ph-locations-api.buonzz.com/v1/cities");
            barangayData = LocationUtils.fetchLocationData("https://ph-locations-api.buonzz.com/v1/barangays");
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception or log the error
        }
    }
}
