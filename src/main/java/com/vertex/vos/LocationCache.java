package com.vertex.vos;

import java.io.IOException;
import java.util.List;

public class LocationCache {
    private static List<String> provinces;
    private static List<String> cities;
    private static List<String> barangays;

    static {
        try {
            provinces = LocationUtils.fetchLocations("https://ph-locations-api.buonzz.com/v1/provinces");
            cities = LocationUtils.fetchLocations("https://ph-locations-api.buonzz.com/v1/cities");
            barangays = LocationUtils.fetchLocations("https://ph-locations-api.buonzz.com/v1/barangays");
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception
        }
    }

    public static List<String> getProvinces() {
        return provinces;
    }

    public static List<String> getCities() {
        return cities;
    }

    public static List<String> getBarangays() {
        return barangays;
    }
}
