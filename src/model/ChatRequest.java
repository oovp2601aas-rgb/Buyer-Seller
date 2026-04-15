package model;

import java.util.Date;

/**
 * ChatRequest Model - Represents a buyer's message/request
 * Part of MVC architecture
 *
 * Enhanced for multi-session support with status tracking
 */
public class ChatRequest {

    // Request status for multi-session tracking
    public enum Status {
        PENDING,    // Just received, not yet responded
        WAITING,    // Seller is working on it
        RESPONDED   // Seller has responded
    }

    private int requestId;
    private String requestLabel; // e.g., "REQ-1", "REQ-2"
    private String buyerMessage;
    private String productExplanation;
    private String priceEstimation;
    private String stockAvailability;
    private Date timestamp;
    private Status status;

    public ChatRequest(int requestId, String buyerMessage) {
        this.requestId = requestId;
        this.requestLabel = "REQ-" + requestId;
        this.buyerMessage = buyerMessage;
        this.timestamp = new Date();
        this.status = Status.PENDING;
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getRequestLabel() {
        return requestLabel;
    }

    public void setRequestLabel(String requestLabel) {
        this.requestLabel = requestLabel;
    }

    public String getBuyerMessage() {
        return buyerMessage;
    }

    public void setBuyerMessage(String buyerMessage) {
        this.buyerMessage = buyerMessage;
    }

    public String getProductExplanation() {
        return productExplanation;
    }

    public void setProductExplanation(String productExplanation) {
        this.productExplanation = productExplanation;
    }

    public String getPriceEstimation() {
        return priceEstimation;
    }

    public void setPriceEstimation(String priceEstimation) {
        this.priceEstimation = priceEstimation;
    }

    public String getStockAvailability() {
        return stockAvailability;
    }

    public void setStockAvailability(String stockAvailability) {
        this.stockAvailability = stockAvailability;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean hasSellerResponse() {
        return productExplanation != null || priceEstimation != null || stockAvailability != null;
    }

    public boolean isFullyResponded() {
        return productExplanation != null && priceEstimation != null && stockAvailability != null;
    }
}