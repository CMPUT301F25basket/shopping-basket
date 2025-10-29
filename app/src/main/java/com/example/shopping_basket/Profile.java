package com.example.shopping_basket;

import java.util.ArrayList;
import java.util.UUID;

public class Profile {
    private String GUID;
    private String name;
    private String phone;
    private String email;
    private ArrayList<Notif> notifs;

    public Profile(String name, String phone, String email){
        GUID = UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
