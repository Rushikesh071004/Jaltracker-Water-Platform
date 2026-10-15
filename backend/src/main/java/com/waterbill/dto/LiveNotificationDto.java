package com.waterbill.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public class LiveNotificationDto {

    private String id;
    private String type; // e.g., "USER_APPROVAL", "USER_REJECTION", "PAYMENT_SUCCESS", "BILL_FINALIZED"
    private String title;
    private String message;
    private Long targetUserId;
    private String targetUsername;
    private OffsetDateTime timestamp;
    private Map<String, Object> payload;

    public LiveNotificationDto() {
        this.timestamp = OffsetDateTime.now();
    }

    public LiveNotificationDto(String id, String type, String title, String message, Long targetUserId, String targetUsername, Map<String, Object> payload) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.targetUserId = targetUserId;
        this.targetUsername = targetUsername;
        this.timestamp = OffsetDateTime.now();
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
