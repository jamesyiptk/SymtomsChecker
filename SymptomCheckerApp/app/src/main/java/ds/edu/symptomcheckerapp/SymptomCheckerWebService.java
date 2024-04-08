/**
 * @author Tsz King Yip
 * Email: tszkingy@andrew.cmu.edu
 * Andrew ID: tszkingy
 */
package ds.edu.symptomcheckerapp;

import android.util.Log;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Web service class to send JSON requests to the symptom checker web service
 * This class sends a JSON request to the symptom checker web service
 * The web service processes the request and returns a JSON response
 * The response is then passed to the response listener
 */
public class SymptomCheckerWebService {

    private static final String WEB_SERVICE_URL = "https://organic-space-tribble-7grxvqvgjv3xgpr-8080.app.github.dev/symptoms";

    private static final String TAG = "SymptomCheckerWebService";

    // Method to send a JSON request to the web service
    public static void sendJsonRequest(JsonObject requestBody, ResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(WEB_SERVICE_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Log the request body
                    String requestBodyString = requestBody.toString();
                    Log.d(TAG, "Request Body: " + requestBodyString);

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(requestBodyString.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    // Get the response code
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Log the response
                        String responseString = response.toString();
                        Log.d(TAG, "Response: " + responseString);

                        listener.onResponse(responseString);
                    } else {
                        // Log the error response code
                        Log.e(TAG, "HTTP error code: " + responseCode);
                        listener.onError("HTTP error code: " + responseCode);
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    // Log the exception
                    Log.e(TAG, "Error: " + e.getMessage());
                    listener.onError(e.getMessage());
                }
            }
        }).start();
    }

    // Interface to handle the response from the web service
    public interface ResponseListener {
        void onResponse(String response);
        void onError(String error);
    }
}