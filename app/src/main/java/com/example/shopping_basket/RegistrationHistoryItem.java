package com.example.shopping_basket;

import com.google.firebase.Timestamp;

public class RegistrationHistoryItem {

    private String eventId;
    private String eventTitle;
    private String status;   // "selected", "not_selected", "revoked"
    private Timestamp timestamp;

    public RegistrationHistoryItem() {
    }

    public RegistrationHistoryItem(String eventId,
                                   String eventTitle,
                                   String status,
                                   Timestamp timestamp) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public String getStatus() { return status; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
