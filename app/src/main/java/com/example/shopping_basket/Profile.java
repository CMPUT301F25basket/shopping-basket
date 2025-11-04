package com.example.shopping_basket;

import java.util.ArrayList;
import java.util.UUID;

public class Profile {
    private String GUID;
    private String name;
    private String phone;
    private String email;

    public Profile(String name, String phone, String email){
        GUID = UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Finds all notifications for the chosen profile
    //Make sure any accepted invites are updated appropriately on the database,
    //not just the local variable
    public ArrayList<Notif> getNotifs(ArrayList<Notif> notifs) {
        ArrayList<Notif> myNotifs = new ArrayList<Notif>();
        for(Notif i : notifs){
            if(i.getTarget().equals(this.GUID)){
                myNotifs.add(i);
            }
        }
        return myNotifs;
    }

    //find organizer's event if it exists
    //make sure that any actions done to this event are done on the database,
    //not just the local variable
    public Event getMyEvent(ArrayList<Event> events) {
        for(Event i : events){
            if(i.getOwner().getGUID().equals(this.GUID)){
                return i;
            }
        }
        //return null if no event found
        return null;
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
