/**
 * @author Tsz King Yip
 * Email: tszkingy@andrew.cmu.edu
 * Andrew ID: tszkingy
 */
package com.example.symptomscheckerservice;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * This class is the model for the Symptoms Checker service. It interacts with the Endless Medical API to analyze symptoms.
 * It provides methods to initialize a session, accept terms of use, update features, and analyze symptoms.
 */
public class SymptomsCheckerModel {
    private static final String API_URL = "https://api.endlessmedical.com/v1/dx";
    private static final Gson gson = new Gson();

    // Initialize a session with the Endless Medical API
    public String initSession() throws Exception {
        URL url = new URL(API_URL + "/InitSession");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(conn);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            return jsonResponse.get("SessionID").getAsString();
        } else {
            throw new Exception("Failed to initialize session");
        }
    }

    // Accept the terms of use of the Endless Medical API
    public void acceptTerms(String sessionId) throws Exception {
        String terms = "I have read, understood and I accept and agree to comply with the Terms of Use of EndlessMedicalAPI and Endless Medical services. The Terms of Use are available on endlessmedical.com";
        String encodedTerms = URLEncoder.encode(terms, "UTF-8");

        URL url = new URL(API_URL + "/AcceptTermsOfUse?SessionID=" + sessionId + "&passphrase=" + encodedTerms);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to accept terms of use");
        }
    }

    // Update a feature in the session
    public void updateFeature(String sessionId, String feature, String value) throws Exception {
        String encodedValue = URLEncoder.encode(value, "UTF-8");
        URL url = new URL(API_URL + "/UpdateFeature?SessionID=" + sessionId + "&name=" + feature + "&value=" + encodedValue);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to update feature: " + feature);
        }
    }

    // Analyze the symptoms in the session and return the diagnosis for response purposes
    public String analyzeSymptoms(String sessionId) throws Exception {
        URL url = new URL(API_URL + "/Analyze?SessionID=" + sessionId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(conn);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            String diagnosis = "";

            // Extract the disease with the highest probability
            JsonArray diseasesArray = jsonResponse.getAsJsonArray("Diseases");
            if (diseasesArray != null && !diseasesArray.isEmpty()) {
                JsonObject diseaseObject = diseasesArray.get(0).getAsJsonObject();
                String diseaseName = diseaseObject.keySet().iterator().next();
                double probability = diseaseObject.get(diseaseName).getAsDouble();
                diagnosis = diseaseName + "," + probability;
            }

            return diagnosis;
        } else {
            throw new Exception("Failed to analyze symptoms");
        }
    }

    // Read the response from the HttpURLConnection
    private String readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    // Get the JSON object from the data
    public JsonObject getJsonObject(JsonObject data) {
        return data;
    }
}