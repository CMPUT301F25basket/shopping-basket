package com.example.shopping_basket;

public class Invite extends Notif{
    private boolean responded;

    public Invite(String target, String message){
        super(target, message);
        responded = false;
    }

    public void respond(){
        responded = true;
    }

    public boolean isResponded() {
        return responded;
    }

    public void setResponded(boolean responded) {
        this.responded = responded;
    }
}
