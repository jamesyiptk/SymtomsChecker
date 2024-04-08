/**
 * @author Tsz King Yip
 * Email: tszkingy@andrew.cmu.edu
 * Andrew ID: tszkingy
 */
package com.example.symptomscheckerservice;

// This class represents a log entry in the MongoDB database
public class LogEntry {
    private String phoneModel;
    private String requestParameters;
    private long requestTimestamp;
    private long apiRequestTimestamp;
    private long apiResponseTimestamp;
    private long replyTimestamp;
    private String symptom;

    // Getters and setters

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(String requestParameters) {
        this.requestParameters = requestParameters;
    }

    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public long getApiRequestTimestamp() {
        return apiRequestTimestamp;
    }

    public void setApiRequestTimestamp(long apiRequestTimestamp) {
        this.apiRequestTimestamp = apiRequestTimestamp;
    }

    public long getApiResponseTimestamp() {
        return apiResponseTimestamp;
    }

    public void setApiResponseTimestamp(long apiResponseTimestamp) {
        this.apiResponseTimestamp = apiResponseTimestamp;
    }

    public long getReplyTimestamp() {
        return replyTimestamp;
    }

    public void setReplyTimestamp(long replyTimestamp) {
        this.replyTimestamp = replyTimestamp;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }
}