package com.example.shopping_basket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

public class ProjectTest {
    private ArrayList<Profile> testProfileData(){
        ArrayList<Profile> testProfiles = new ArrayList<Profile>();
        testProfiles.add(new Profile("Bob", "8675309", "generic@nmail.com"));
        testProfiles.add(new Profile("Alice", "010101", "genericer@nmail.com"));
        testProfiles.add(new Profile("John", "123456", "genericest@nmail.com"));
        testProfiles.add(new Profile("Nhoj", "654321", "genericerer@nmail.com"));
        testProfiles.get(1).setGUID("1");
        testProfiles.get(2).setGUID("2");
        testProfiles.get(3).setGUID("3");
        return testProfiles;
    }

    private ArrayList<Event> testEventData(ArrayList<Profile> profileData){
        ArrayList<Event> testEvents = new ArrayList<Event>();
        testEvents.add(new Event(profileData.get(1), "Test Event", "This is a test event", 2, 3, null, null));
        testEvents.add(new Event(profileData.get(3), "Test Event 2", "This is a test event", 3, 0, null, null));
        return testEvents;
    }

    private ArrayList<Notif> testNotifData(){
        ArrayList<Notif> testNotif = new ArrayList<Notif>();
        testNotif.add(new Notif("1", "Test notification"));
        testNotif.add(new Notif("2", "Test notification"));
        testNotif.add(new Invite("2", "Test Invite"));
        return testNotif;
    }

    //Profile tests
    @Test
    public void testGetNotifs(){
        ArrayList<Notif> notifs = testNotifData();
        ArrayList<Profile> profiles = testProfileData();
        assertEquals(1, profiles.get(1).getNotifs(notifs).size());
        assertEquals(2, profiles.get(2).getNotifs(notifs).size());
        assertEquals(0, profiles.get(0).getNotifs(notifs).size());
    }

    @Test
    public void testGetMyEvent(){
        ArrayList<Profile> profiles = testProfileData();
        ArrayList<Event> events = testEventData(profiles);
        Event check = profiles.get(1).getMyEvent(events);
        assertEquals(2, check.getSelectNum());
        assertEquals(3, check.getMaxReg());
        check = profiles.get(0).getMyEvent(events);
        assertNull(check);
        check = profiles.get(3).getMyEvent(events);
        assertEquals("Test Event 2", check.getName());
        assertEquals("This is a test event", check.getDesc());
    }

    //Event tests
    @Test
    public void testJoinEvent(){
        ArrayList<Profile> profiles = testProfileData();
        ArrayList<Event> events = testEventData(profiles);
        events.get(0).joinEvent(profiles.get(0));
        events.get(0).joinEvent(profiles.get(1));
        events.get(0).joinEvent(profiles.get(2));
        events.get(0).joinEvent(profiles.get(3));
        events.get(1).joinEvent(profiles.get(0));
        events.get(1).joinEvent(profiles.get(0));
        assertEquals(3, events.get(0).getWaitListSize());
        assertEquals(1, events.get(1).getWaitListSize());
    }

    @Test
    public void testLeaveEvent(){
        ArrayList<Profile> profiles = testProfileData();
        ArrayList<Event> events = testEventData(profiles);
        events.get(0).joinEvent(profiles.get(0));
        events.get(0).joinEvent(profiles.get(1));
        events.get(0).joinEvent(profiles.get(2));
        events.get(0).joinEvent(profiles.get(3));
        events.get(1).joinEvent(profiles.get(0));
        events.get(1).joinEvent(profiles.get(0));
        events.get(0).leaveEvent(profiles.get(2));
        events.get(0).leaveEvent(profiles.get(3));
        events.get(0).leaveEvent(profiles.get(1));
        events.get(1).leaveEvent(profiles.get(0));
        events.get(1).leaveEvent(profiles.get(2));
        assertEquals(1, events.get(0).getWaitListSize());
        assertEquals(2, events.get(0).getCancelList().size());
        assertEquals(0, events.get(1).getWaitListSize());
        assertEquals(1, events.get(1).getCancelList().size());
    }

    @Test
    public void testLottery(){
        ArrayList<Profile> profiles = testProfileData();
        ArrayList<Event> events = testEventData(profiles);
        events.get(0).joinEvent(profiles.get(0));
        events.get(0).joinEvent(profiles.get(1));
        events.get(0).joinEvent(profiles.get(2));
        events.get(0).joinEvent(profiles.get(3));
        events.get(1).joinEvent(profiles.get(0));

        //should only be 2 winners, 1 loser , and 2 invites sent
        ArrayList<Invite> invites = events.get(0).runLottery();
        assertEquals(2, events.get(0).getInviteList().size());
        assertEquals(1, events.get(0).getWaitListSize());
        assertEquals(2, invites.size());

        //Second run should not change lists and not produce invites
        invites = events.get(0).runLottery();
        assertEquals(2, events.get(0).getInviteList().size());
        assertNull(invites);

        //should invite sole waiting list resident
        invites = events.get(1).runLottery();
        assertEquals(1, events.get(1).getInviteList().size());
        assertEquals(1, invites.size());

        //Should fill remaining slots and produce only 2 invites
        events.get(1).joinEvent(profiles.get(1));
        events.get(1).joinEvent(profiles.get(2));
        events.get(1).joinEvent(profiles.get(3));
        invites = events.get(1).runLottery();
        assertEquals(3, events.get(1).getInviteList().size());
        assertEquals(1, events.get(1).getWaitListSize());
        assertEquals(2, invites.size());
    }

    @Test
    public void testEnroll(){
        ArrayList<Profile> profiles = testProfileData();
        ArrayList<Event> events = testEventData(profiles);
        events.get(0).joinEvent(profiles.get(0));
        events.get(0).runLottery();
        events.get(0).enroll(profiles.get(0));
        assertEquals(1, events.get(0).getEnrollList().size());
        assertEquals(0, events.get(0).getWaitListSize());

        //check that users can only enroll once
        events.get(0).enroll(profiles.get(0));
        assertEquals(1, events.get(0).getEnrollList().size());
        assertEquals(0, events.get(0).getWaitListSize());

        //check that uninvited users cannot enroll
        events.get(0).enroll(profiles.get(1));
        assertEquals(1, events.get(0).getEnrollList().size());
        assertEquals(0, events.get(0).getWaitListSize());
    }

    @Test
    public void testDecline(){
        ArrayList<Profile> profiles = testProfileData();
        ArrayList<Event> events = testEventData(profiles);
        events.get(0).joinEvent(profiles.get(0));
        events.get(0).runLottery();
        events.get(0).decline(profiles.get(0));
        assertEquals(1, events.get(0).getCancelList().size());
        assertEquals(0, events.get(0).getWaitListSize());

        //check that users can only decline once
        events.get(0).decline(profiles.get(0));
        assertEquals(1, events.get(0).getCancelList().size());
        assertEquals(0, events.get(0).getWaitListSize());

        //check that only invited users can decline
        events.get(0).decline(profiles.get(1));
        assertEquals(1, events.get(0).getCancelList().size());
        assertEquals(0, events.get(0).getWaitListSize());
    }

    @Test
    public void testNotify(){
        ArrayList<Profile> profiles = testProfileData();
        ArrayList<Event> events = testEventData(profiles);
        events.get(0).joinEvent(profiles.get(0));
        events.get(0).joinEvent(profiles.get(1));
        events.get(0).joinEvent(profiles.get(2));
        ArrayList<Notif> notifs = new ArrayList<Notif>();
        notifs = events.get(0).notifyWaiting("Hello");
        assertEquals(3, notifs.size());
        assertEquals("Hello", notifs.get(0).getMessage());
        assertEquals("1", notifs.get(1).getTarget());
        assertEquals("2", notifs.get(2).getTarget());
        events.get(1).joinEvent(profiles.get(3));
        events.get(1).runLottery();
        notifs = events.get(1).notifyInvited("Hello");
        assertEquals(1, notifs.size());
    }
}
