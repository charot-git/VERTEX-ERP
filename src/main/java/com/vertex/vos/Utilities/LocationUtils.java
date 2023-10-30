package com.vertex.vos.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LocationUtils {

    public static Map<String, String> fetchLocationData(String apiUrl) throws IOException {
        Map<String, String> locationData = new HashMap<>();

        int page = 1;
        boolean hasNextPage = true;

        while (hasNextPage) {
            String apiUrlWithPage = apiUrl + "?page=" + page;
            URL url = new URL(apiUrlWithPage);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONArray data = getObjects(connection);

                for (int i = 0; i < data.length(); i++) {
                    JSONObject locationObj = data.getJSONObject(i);
                    String id = locationObj.getString("id");
                    String name = locationObj.getString("name");
                    locationData.put(id, name);
                }

                // Check if there are more pages
                hasNextPage = data.length() > 0;
                page++;
            } else {
                // Handle API error or unexpected response code
                // You might want to throw an exception or log the error
                System.err.println("Failed to fetch provinces. HTTP Response Code: " + responseCode);
                break;
            }

            // Close the connection after each page's data is fetched
            connection.disconnect();
        }

        return locationData;
    }

    private static JSONArray getObjects(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Parse the JSON response and extract the location names and codes
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray data = jsonResponse.getJSONArray("data");
        return data;
    }
}
