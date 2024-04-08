/**
 * @author Tsz King Yip
 * Email: tszkingy@andrew.cmu.edu
 * Andrew ID: tszkingy
 */
package com.example.symptomscheckerservice;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.util.logging.Logger;
import java.time.LocalDateTime;

/**
 * This servlet receives a JSON object from the Android app, processes the data, and sends a response back to the app.
 * It uses the SymptomsCheckerModel to interact with the Endless Medical API.
 * It also logs the request and response timestamps, and stores the log entry in the MongoDB database.
 */
@WebServlet(urlPatterns = {"/symptoms"})
public class SymptomsCheckerServlet extends HttpServlet {
    SymptomsCheckerModel model;

    // Create a Logger instance for this class
    private static final Logger logger = Logger.getLogger(SymptomsCheckerServlet.class.getName());

    // Initiate this servlet by instantiating the model that it will use
    public void init() {
        model = new SymptomsCheckerModel();
    }

    // Handle POST requests from the Android app
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Read the JSON data from the request body
        BufferedReader reader = request.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        // Log the received JSON data
        logger.info("Received JSON data: " + requestBody.toString());

        // Parse the JSON data
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(requestBody.toString(), JsonObject.class);

        // Process the received data
        JsonObject myJsonObject = model.getJsonObject(jsonObject);
        // Extract relevant information from the request
        String phoneModel = request.getHeader("User-Agent");
        String requestParameters = jsonObject.toString();

        LocalDateTime now = LocalDateTime.now();
        long requestTimestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        try {
            // Initialize session
            String sessionId = model.initSession();

            // Accept terms of use
            model.acceptTerms(sessionId);


            // Update features
            for (String feature : myJsonObject.keySet()) {
                String value = myJsonObject.get(feature).getAsString();
                model.updateFeature(sessionId, feature, value);
            }

            // Log the API request and response timestamps
            LocalDateTime apiRequestTime = LocalDateTime.now();
            long apiRequestTimestamp = apiRequestTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Analyze symptoms
            String diagnosis = model.analyzeSymptoms(sessionId);
            LocalDateTime apiResponseTime = LocalDateTime.now();
            long apiResponseTimestamp = apiResponseTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Log the reply timestamp
            LocalDateTime replyTime = LocalDateTime.now();
            long replyTimestamp = replyTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();


            // Create a LogEntry instance
            LogEntry logEntry = new LogEntry();
            logEntry.setPhoneModel(phoneModel);
            logEntry.setRequestParameters(requestParameters);
            logEntry.setRequestTimestamp(requestTimestamp);
            logEntry.setApiRequestTimestamp(apiRequestTimestamp);
            logEntry.setApiResponseTimestamp(apiResponseTimestamp);
            logEntry.setReplyTimestamp(replyTimestamp);
            logEntry.setSymptom(diagnosis.split(",")[0]);

            // Store the log entry in the MongoDB database
            MongoDBConnection.storeLogEntry(logEntry);

            // Create JSON response
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("diagnosis", diagnosis);

            // Send a response back to the Android app
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(jsonResponse));
            out.flush();

            logger.info("Response sent: " + gson.toJson(jsonResponse));
        } catch (Exception e) {
            logger.severe("Processing error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Processing error");
        }
    }
}
