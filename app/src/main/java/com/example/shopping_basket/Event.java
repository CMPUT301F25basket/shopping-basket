package com.example.shopping_basket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * This class defines a user created event
 * and tracks all registrants
 */
public class Event {
    private Profile owner;
    private String name;
    private String desc;
    private int selectNum;
    //
    private int maxReg;
    private Calendar startDate;
    private Calendar endDate;
    //private ----- poster;
    private ArrayList<Profile> waitingList;
    private ArrayList<Profile> inviteList;
    private ArrayList<Profile> enrollList;
    private ArrayList<Profile> cancelList;

    public Event(Profile owner, String name, String desc, int selectNum, int maxReg, Calendar startDate, Calendar endDate){
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.selectNum = selectNum;
        this.maxReg = maxReg;
        this.startDate = startDate;
        this.endDate = endDate;
        this.waitingList = new ArrayList<Profile>();
        this.inviteList = new ArrayList<Profile>();
        this.enrollList = new ArrayList<Profile>();
        this.cancelList = new ArrayList<Profile>();
    }

    /**
     * this method adds the given profile to the event waiting list
     * @param profile
     *     profile to be added
     */
    public void joinEvent(Profile profile){
        //check if waiting list is at capacity
        if((waitingList.size() >= maxReg) && (maxReg != 0)){
            //show app error that registration is full
            return;
        }
        //check if user is already registered
        for(int i = 0; i < waitingList.size(); i++){
            if(waitingList.get(i).getGUID().equals(profile.getGUID())){
                //show code error that user is already registered
                //**maybe return 1 on success, -1 on fail**
                return;
            }
        }
        //check if user previously cancelled
        for(int i = 0; i < cancelList.size(); i++){
            if(cancelList.get(i).getGUID().equals(profile.getGUID())){
                //remove user from cancel list
                cancelList.remove(i);
                break;
            }
        }
        //otherwise, add user to waiting list
        waitingList.add(profile);
    }

    /**
     * This method is for removing a user that cancels their registration
     * from the waiting list and placing them in the cancelled list
     * @param profile
     *     profile to be moved
     */
    public void leaveEvent(Profile profile){
        //check that user is in waiting list
        for(int i = 0; i < waitingList.size(); i++){
            if(waitingList.get(i).getGUID().equals(profile.getGUID())){
                //if user found, transfer from invite list to enrolled
                cancelList.add(profile);
                waitingList.remove(i);
                return;
            }
        }
        //if not, return code error
    }

    /**
     * This method runs the lottery to choose users in the waiting list
     * up to the max number desired, then returns invites for database
     * Functions as long as there is empty slots to fill,
     * can be called multiple times
     * @return
     *     Array of invites to be added to database
     */
    public ArrayList<Invite> runLottery(){
        int slots = selectNum - (inviteList.size() + enrollList.size());
        //account for possible errors with altered selectNum value
        if(slots <= 0){
            //return app error that all slots are currently filled
            return null;
        }
        //if waiting list is empty, return null
        if(waitingList.isEmpty()){
            return null;
        }

        ArrayList<Invite> invites = new ArrayList<Invite>();
        String message = "You have been invited to enroll in " + name;
        //if there are enough slots for everyone in the lottery, simply take them all
        if(slots >= waitingList.size()){
            inviteList.addAll(waitingList);
            for(Profile i : waitingList){
                invites.add(new Invite(i.getGUID(), message));
            }
            waitingList.clear();
            return invites;
        }

        //otherwise commence lottery
        Random lottery = new Random();
        int winner;
        for(int i = 0; i < slots; i++){
            winner = lottery.nextInt(waitingList.size());
            inviteList.add(waitingList.get(winner));
            invites.add(new Invite(waitingList.get(winner).getGUID(), message));
            waitingList.remove(winner);
        }
        return invites;
    }

    /**
     * This method moves a profile from the waiting list to the enroll list
     * when a user accepts an invite
     * @param profile
     *     profile to move
     */
    public void enroll(Profile profile){
        //check that user was invited and was not uninvited
        for(int i = 0; i < inviteList.size(); i++){
            if(inviteList.get(i).getGUID().equals(profile.getGUID())){
                //if user found, transfer from invite list to enrolled
                enrollList.add(profile);
                inviteList.remove(i);
                return;
            }
        }
        //if not, return app error that invitation has expired
    }

    /**
     * this method moves invited profiles from the waiting list to the cancelled list
     * either due to a user rejecting an invite, or the organizer removing them
     * @param profile
     *     profile t be moved
     */
    public void decline(Profile profile){
        //check that user was invited
        for(int i = 0; i < inviteList.size(); i++){
            if(inviteList.get(i).getGUID().equals(profile.getGUID())){
                //if user found, transfer from invite list to cancelled
                cancelList.add(profile);
                inviteList.remove(i);
                return;
            }
        }
        //if not, return code error
    }

    /**
     * This methods sends a general notifications to all profiles in the waiting list
     * @param string
     *     message to be sent in notification
     * @return
     *     array of notif objects to be added to database
     */
    public ArrayList<Notif> notifyWaiting(String string){
        ArrayList<Notif> notifyList = new ArrayList<Notif>();
        for(Profile i : waitingList){
            notifyList.add(new Notif(i.getGUID(), string));
        }
        return notifyList;
    }

    /**
     * This methods sends a general notifications to all profiles in the invite list
     * @param string
     *     message to be sent in notification
     * @return
     *     array of notif objects to be added to database
     */
    public ArrayList<Notif> notifyInvited(String string){
        ArrayList<Notif> notifyList = new ArrayList<Notif>();
        for(Profile i : inviteList){
            notifyList.add(new Notif(i.getGUID(), string));
        }
        return notifyList;
    }

    /**
     * This methods sends a general notifications to all profiles in the enrolled list
     * @param string
     *     message to be sent in notification
     * @return
     *     array of notif objects to be added to database
     */
    public ArrayList<Notif> notifyEnrolled(String string){
        ArrayList<Notif> notifyList = new ArrayList<Notif>();
        for(Profile i : enrollList){
            notifyList.add(new Notif(i.getGUID(), string));
        }
        return notifyList;
    }

    /**
     * This methods sends a general notifications to all profiles in the cancelled list
     * @param string
     *     message to be sent in notification
     * @return
     *     array of notif objects to be added to database
     */
    public ArrayList<Notif> notifyCancelled(String string){
        ArrayList<Notif> notifyList = new ArrayList<Notif>();
        for(Profile i : cancelList){
            notifyList.add(new Notif(i.getGUID(), string));
        }
        return notifyList;
    }

    public Profile getOwner() {
        return owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSelectNum() {
        return selectNum;
    }

    public void setSelectNum(int selectNum) {
        this.selectNum = selectNum;
    }

    public int getMaxReg() {
        return maxReg;
    }

    public void setMaxReg(int maxReg) {
        this.maxReg = maxReg;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public ArrayList<Profile> getWaitingList() {
        return waitingList;
    }

    public int getWaitListSize() {
        return waitingList.size();
    }

    public ArrayList<Profile> getInviteList() {
        return inviteList;
    }

    public ArrayList<Profile> getEnrollList() {
        return enrollList;
    }

    public ArrayList<Profile> getCancelList() {
        return cancelList;
    }
}
