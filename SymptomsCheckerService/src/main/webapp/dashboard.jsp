<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.symptomscheckerservice.LogEntry" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.Instant" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.ZoneId" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.JsonObject" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
    <style>
        table {
            border-collapse: collapse;
            width: 100%;
        }
        th, td {
            border: 1px solid black;
            padding: 8px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
<h1>Dashboard</h1>

<h2>Operations Analytics</h2>
<table>
    <tr>
        <th>Metric</th>
        <th>Value</th>
    </tr>
    <tr>
        <td>Total Requests</td>
        <td>${totalRequests}</td>
    </tr>
    <tr>
        <td>Average API Response Time (ms)</td>
        <td>${averageApiResponseTime}</td>
    </tr>
    <tr>
        <td>Average Age</td>
        <td>${averageAge}</td>
    </tr>
    <tr>
        <td>Most Frequent Symptom</td>
        <td>${mostFrequentSymptom}</td>
    </tr>
    <!-- Add more rows for other analytics metrics -->
</table>

<h2>Log Entries</h2>
<table>
    <tr>
        <th>Phone Model</th>
        <th>Request Parameters</th>
        <th>Request Timestamp</th>
        <th>API Request Timestamp</th>
        <th>API Response Timestamp</th>
        <th>Reply Timestamp</th>
    </tr>
    <%
        Gson gson = new Gson();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<LogEntry> logEntries = (List<LogEntry>) request.getAttribute("logEntries");
        for (LogEntry logEntry : logEntries) {
    %>
    <tr>
        <td>
            <%
                String phoneModel = logEntry.getPhoneModel();
                String[] phoneModelParts = phoneModel.split("[()]");
                for (String part : phoneModelParts) {
                    if (!part.isEmpty()) {
            %>
            <%= part.trim() %><br>
            <%
                    }
                }
            %>
        </td>
        <td>
            <%
                JsonObject jsonObject = gson.fromJson(logEntry.getRequestParameters(), JsonObject.class);
                for (String key : jsonObject.keySet()) {
            %>
            <%= key %>: <%= jsonObject.get(key).getAsString() %><br>
            <%
                }
            %>
        </td>
        <td><%= formatTimestamp(logEntry.getRequestTimestamp(), formatter) %></td>
        <td><%= formatTimestamp(logEntry.getApiRequestTimestamp(), formatter) %></td>
        <td><%= formatTimestamp(logEntry.getApiResponseTimestamp(), formatter) %></td>
        <td><%= formatTimestamp(logEntry.getReplyTimestamp(), formatter) %></td>
    </tr>
    <%
        }
    %>
</table>
</body>
</html>

<%!
    private String formatTimestamp(long timestampMillis, DateTimeFormatter formatter) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
        return dateTime.format(formatter);
    }
%>