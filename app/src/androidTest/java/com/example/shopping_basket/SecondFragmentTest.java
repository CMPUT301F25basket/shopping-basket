package com.example.shopping_basket;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static
        androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
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
public class SecondFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testSecondFragment() {
        // Starting Point
        onView(withId(R.id.button_first)).perform(click());

        onView(withId(R.id.button_second)).check(matches(isDisplayed()));
    }

    @Test
    public void testAction_SecondFragment_to_FirstFragment() {

        // Starting Point
        onView(withId(R.id.button_first)).perform(click());

        // Test click (from SecondFragment)
        onView(withId(R.id.button_second)).perform(click());

        // Test Display (to FirstFragment)
        onView(withId(R.id.button_first)).check(matches(isDisplayed()));
    }

}
