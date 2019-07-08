package com.gitlab.kreikenbaum.suntime.data;


import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TransitCalculatorTest {

    // http://api.usno.navy.mil/rstt/oneday?date=7/4/2018&coords=41.89N,12.48E
    @Test
    public void test_fixed() {
        TransitCalculator tc = new TransitCalculator();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 2018);
        c.set(Calendar.MONTH, Calendar.JULY);
        c.set(Calendar.DAY_OF_MONTH, 4);
        c.set(Calendar.HOUR_OF_DAY, 12);
        tc.calculateTransit(c.getTimeInMillis(), 41.890000, 12.480000);
        Calendar cTransit = Calendar.getInstance();
        cTransit.setTimeZone(TimeZone.getTimeZone("UTC"));
        cTransit.setTimeInMillis(tc.transit);
        assertEquals(11, cTransit.get(Calendar.HOUR_OF_DAY));
        assertEquals(14, cTransit.get(Calendar.MINUTE), 1);
        // paranoia below
        assertEquals(2018, cTransit.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, cTransit.get(Calendar.MONTH));
    }

    // todo: computeAt from api-code, computeAt in mainactivity, check that delta == 0 or 1 works
}
