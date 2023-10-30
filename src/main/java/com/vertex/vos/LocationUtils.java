package com.vertex.vos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocationUtils {

    public static List<String> fetchLocations(String apiUrl) throws IOException {
        List<String> locations = new ArrayList<>();

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            JSONArray data = getObjects(connection);

            for (int i = 0; i < data.length(); i++) {
                JSONObject locationObj = data.getJSONObject(i);
                String name = locationObj.getString("name");
                locations.add(name);
            }
        }

        return locations;
    }

    private static JSONArray getObjects(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Parse the JSON response and extract the location names
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray data = jsonResponse.getJSONArray("data");
        return data;
    }
}
