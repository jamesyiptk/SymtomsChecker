/**
 * @author Tsz King Yip
 * Email: tszkingy@andrew.cmu.edu
 * Andrew ID: tszkingy
 */
package com.example.symptomscheckerservice;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * This class is responsible for connecting to the MongoDB database and storing log entries.
 */
public class MongoDBConnection {
    private static final String DB_NAME = "LogDatabase";
    private static final String COLLECTION_NAME = "log_entries";
    private static final String MONGO_URI = "mongodb://tszkingy:khRWK21pXBZ1b1M2@ac-uyekco2-shard-00-02.eqvt5lk.mongodb.net:27017,ac-uyekco2-shard-00-01.eqvt5lk.mongodb.net:27017,ac-uyekco2-shard-00-00.eqvt5lk.mongodb.net:27017/LogDatabase?w=majority&retryWrites=true&tls=true&authMechanism=SCRAM-SHA-1";

    // Store a log entry in the MongoDB database
    public static void storeLogEntry(LogEntry logEntry) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DB_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Create a document to store the log entry
            Document document = new Document();
            document.append("phoneModel", logEntry.getPhoneModel())
                    .append("requestParameters", logEntry.getRequestParameters())
                    .append("requestTimestamp", logEntry.getRequestTimestamp())
                    .append("apiRequestTimestamp", logEntry.getApiRequestTimestamp())
                    .append("apiResponseTimestamp", logEntry.getApiResponseTimestamp())
                    .append("replyTimestamp", logEntry.getReplyTimestamp())
                    .append("symptom", logEntry.getSymptom());

            collection.insertOne(document);
        }
    }
}