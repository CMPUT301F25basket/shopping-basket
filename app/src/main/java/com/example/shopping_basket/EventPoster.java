package com.example.shopping_basket;

/**
 * Lightweight view model for posters stored inside event documents.
 * We do NOT use Firebase Storage. The image data is stored as a Base64
 * string on the event document itself.
 */
public class EventPoster {
    private String eventId;
    private String eventName;
    private String posterBase64;
    private String uploaderName;

    public EventPoster() { }

    public EventPoster(String eventId,
                       String eventName,
                       String posterBase64,
                       String uploaderName) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.posterBase64 = posterBase64;
        this.uploaderName = uploaderName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getPosterBase64() {
        return posterBase64;
    }

    public void setPosterBase64(String posterBase64) {
        this.posterBase64 = posterBase64;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }
}