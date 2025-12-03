package com.example.shopping_basket;
import java.util.Date;

/**
 * This class defines a notification sent to a user
 * Named to avoid conflict with existing Notification class
 */
public class Notif {
    // target is GUID of profile notif is sent to
    private String target;
    private String message;
    private Date time;

    public Notif() {}

    public Notif(String target, String message){
        this.target = target;
        this.message = message;
        this.time = new Date();
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTime() { return time; }
    public void setTime(Date time) {
        this.time = time;
    }
}
