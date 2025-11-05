package com.example.shopping_basket;

/**
 * Notif subclass for invites specifically
 */
public class Invite extends Notif{
    private boolean responded;

    public Invite(String target, String message){
        super(target, message);
        responded = false;
    }

    /**
     * This method is used to set the indictor that a response has been given
     */
    public void respond(){
        responded = true;
    }

    public boolean getResponded() {
        return responded;
    }

    public void setResponded(boolean responded) {
        this.responded = responded;
    }
}
