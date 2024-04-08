/**
 * @author Tsz King Yip
 * Email: tszkingy@andrew.cmu.edu
 * Andrew ID: tszkingy
 */
package com.example.symptomscheckerservice;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;

import java.io.IOException;
import java.util.*;


/**
 * This servlet is responsible for displaying the dashboard of the Symptoms Checker service.
 * It retrieves log entries from the MongoDB database and calculates statistics to display on the dashboard.
 */
@WebServlet(urlPatterns = {"/"})
public class DashboardServlet extends HttpServlet {
    private static final String DB_NAME = "LogDatabase";
    private static final String COLLECTION_NAME = "log_entries";
    private static final String MONGO_URI = "mongodb://tszkingy:khRWK21pXBZ1b1M2@ac-uyekco2-shard-00-02.eqvt5lk.mongodb.net:27017,ac-uyekco2-shard-00-01.eqvt5lk.mongodb.net:27017,ac-uyekco2-shard-00-00.eqvt5lk.mongodb.net:27017/LogDatabase?w=majority&retryWrites=true&tls=true&authMechanism=SCRAM-SHA-1";

    // Handle GET requests to the dashboard
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<LogEntry> logEntries = retrieveLogEntries();

        int totalRequests = logEntries.size();
        long totalApiResponseTime = 0;
        Map<String, Integer> symptomFrequency = new HashMap<>();
        int ageSum = 0;

        // Calculate statistics
        for (LogEntry logEntry : logEntries) {
            long apiResponseTime = logEntry.getApiResponseTimestamp() - logEntry.getApiRequestTimestamp();
            totalApiResponseTime += apiResponseTime;

            // Count the frequency of each symptom
            String symptom = logEntry.getSymptom();
            symptomFrequency.put(symptom, symptomFrequency.getOrDefault(symptom, 0) + 1);

            // Extract age from request parameters
            String requestParameters = logEntry.getRequestParameters();
            try {
                JsonObject jsonObject = new Gson().fromJson(requestParameters, JsonObject.class);
                if (jsonObject.has("Age")) {
                    ageSum += jsonObject.get("Age").getAsInt();
                }
            } catch (JsonSyntaxException e) {
                // Ignore invalid JSON
            }

        }

        // Calculate average statistics
        double averageApiResponseTime = (double) totalApiResponseTime / totalRequests;
        int averageAge = totalRequests > 0 ? ageSum / totalRequests : 0;

        String mostFrequentSymptom = "Not available";

        // Find the most frequent symptom
        if (!symptomFrequency.isEmpty()) {
            mostFrequentSymptom = Collections.max(symptomFrequency.entrySet(), Map.Entry.comparingByValue()).getKey();
        }

        request.setAttribute("totalRequests", totalRequests);
        request.setAttribute("averageApiResponseTime", averageApiResponseTime);
        request.setAttribute("mostFrequentSymptom", mostFrequentSymptom);
        request.setAttribute("logEntries", logEntries);
        request.setAttribute("averageAge", averageAge);

        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }


    // Retrieve log entries from the MongoDB database
    private List<LogEntry> retrieveLogEntries() {
        List<LogEntry> logEntries = new ArrayList<>();
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DB_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Retrieve all log entries from the database
            for (Document document : collection.find()) {
                LogEntry logEntry = new LogEntry();
                logEntry.setPhoneModel(document.getString("phoneModel"));
                logEntry.setRequestParameters(document.getString("requestParameters"));
                logEntry.setRequestTimestamp(document.getLong("requestTimestamp"));
                logEntry.setApiRequestTimestamp(document.getLong("apiRequestTimestamp"));
                logEntry.setApiResponseTimestamp(document.getLong("apiResponseTimestamp"));
                logEntry.setReplyTimestamp(document.getLong("replyTimestamp"));
                logEntry.setSymptom(document.getString("symptom"));

                logEntries.add(logEntry);
            }
        }

        return logEntries;
    }
}