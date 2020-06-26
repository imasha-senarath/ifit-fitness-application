package com.example.ifitcoach;

import android.view.View;

import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class MainActivityTest
{
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    private MainActivity mainActivity = null;

    @Before
    public void setUp() throws Exception
    {
        mainActivity = mainActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch()
    {
        View view = mainActivity.findViewById(R.id.main_frame);

        assertNotNull(view);
    }

    @After
    public void tearDown() throws Exception
    {
        mainActivity = null;
    }
}