package com.example.shopping_basket;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);


    @Test
    public void testToolbar() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testNav_host_fragment_content_main() {
        onView(withId(R.id.nav_host_fragment_content_main)).check(matches(isDisplayed()));
    }


    @Test
    public void testBottomNav() {
        // Event Creation
        onView(withId(R.id.myEvent)).perform(click());
        onView(withId(R.id.button_create_event)).check(matches(isDisplayed()));

        // Inbox
        onView(withId(R.id.inbox)).perform(click());
        onView(withId(R.id.inboxFragment)).check(matches(isDisplayed()));

        // Home
        onView(withId(R.id.home)).perform(click());
        onView(withId(R.id.event_card_list)).check(matches(isDisplayed()));
    }

    @Test
    public void testHomeFragment() {
        onView(withId(R.id.homeFragment)).check(matches(isDisplayed()));
    }


}