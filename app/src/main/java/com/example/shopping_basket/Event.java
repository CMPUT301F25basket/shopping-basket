package com.example.shopping_basket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

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
    }

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
        //**maybe return 1 on success, -1 on fail**
    }


    //Run lottery and notify winners with invites
    public void runLottery(){
        int slots = selectNum - (inviteList.size() + enrollList.size());
        //account for possible errors with altered selectNum value
        if(slots <= 0){
            //return app error that all slots are currently filled
            return;
        }
        //if waiting list is empty, return
        if(waitingList.isEmpty()){
            return;
        }
        //if there are enough slots for everyone in the lottery, simply take them all
        ArrayList<Profile> newInvite = new ArrayList<Profile>();
        if(slots >= waitingList.size()){
            newInvite.addAll(waitingList);
            inviteList.addAll(waitingList);
            waitingList.clear();
            return;
        }
        //otherwise commence lottery
        Random lottery = new Random();
        int winner;
        for(int i = 0; i < slots; i++){
            winner = lottery.nextInt(waitingList.size());
            inviteList.add(waitingList.get(winner));
            waitingList.remove(winner);
        }
    }

    //call on accept by entrant
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
        //**maybe return 1 on success, -1 on fail**
    }

    //call on decline by entrant or cancelled invitation by organizer
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
        //**maybe return 1 on success, -1 on fail**
    }

    //Create and return array of targeted notifications to be added to database
    //For waiting list registrants
    public ArrayList<Notif> notifyWaiting(String string){
        ArrayList<Notif> notifyList = new ArrayList<Notif>();
        for(Profile i : waitingList){
            notifyList.add(new Notif(i.getGUID(), string));
        }
        return notifyList;
    }

    //Create and return array of targeted notifications to be added to database
    //For invited registrants
    public ArrayList<Notif> notifyInvited(String string){
        ArrayList<Notif> notifyList = new ArrayList<Notif>();
        for(Profile i : inviteList){
            notifyList.add(new Notif(i.getGUID(), string));
        }
        return notifyList;
    }

    //Create and return array of targeted notifications to be added to database
    //For enrolled registrants
    public ArrayList<Notif> notifyEnrolled(String string){
        ArrayList<Notif> notifyList = new ArrayList<Notif>();
        for(Profile i : enrollList){
            notifyList.add(new Notif(i.getGUID(), string));
        }
        return notifyList;
    }

    //Create and return array of targeted notifications to be added to database
    //For cancelled registrants
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

    public int getWaitListSize() {
        return waitingList.size();
    }
}
