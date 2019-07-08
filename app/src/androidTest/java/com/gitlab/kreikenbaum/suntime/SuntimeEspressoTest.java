package com.gitlab.kreikenbaum.suntime;


import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class SuntimeEspressoTest {
    @Rule
    public ActivityTestRule<SunTimeActivity> activityTestRule = new ActivityTestRule<>(SunTimeActivity.class);

    @Test
    public void openSettings() {
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.tv_alarm_time)).check(matches(isDisplayed()));
    }
}
