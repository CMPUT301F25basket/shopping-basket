package com.example.shopping_basket;

import java.util.Calendar;

public class Notif {
    // target is GUID of profile notif is sent to
    private String target;
    private String message;
    //private Calendar time;

    public Notif(String target, String message){
        this.target = target;
        this.message = message;
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
}
