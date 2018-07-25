/* Copyright (C) 2018 gitlab.com/kreikenbaum
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.gitlab.kreikenbaum.suntime.data;

import android.location.Location;

import org.junit.Test;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class SolarTimeTest {
    private static final double latitude = 41.89;
    private static final double longitude = 12.48;
    private static final Location location = MockLocation.fromLatLon(latitude, longitude);

    // upper solar transit to UTC on 7/4/2018 is 11:14 im GMT

    @Test
    public void toTimezoneString() {
        SolarTime solarTime = new SolarTime(2, 3);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 3);
        assertEquals(String.format("GMT+%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)), solarTime.toTimezoneString());
    }

    // getCalendar no longer uses current time
//    @Test
//    public void calendar_creation_works() {
//        SolarTime solarTime = new SolarTime(2, 3);
//        Calendar c = solarTime.getCalendar();
//        assertEquals(System.currentTimeMillis(), c.getTimeInMillis(), 100);
//    }

    @Test
    public void calendar_timezone_change() {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        int hourSomewhere = c.get(Calendar.HOUR_OF_DAY);
        c.setTimeZone(TimeZone.getTimeZone("GMT+12"));
        c.add(Calendar.HOUR_OF_DAY, -12);
        assertEquals(hourSomewhere, c.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void calendar_timezone_small_change() {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        int minuteSomewhere = c.get(Calendar.MINUTE);
        c.setTimeZone(TimeZone.getTimeZone("GMT+00:12"));
        c.add(Calendar.MINUTE, -12);
        assertEquals(minuteSomewhere, c.get(Calendar.MINUTE));
    }

    @Test
    public void getCalendar() {
        SolarTime solarTime = new SolarTime(2, 3);
        Calendar c = solarTime.getCalendar();
        assertEquals(String.format("GMT+%02d:%02d", 2, 3), c.getTimeZone().getID());
    }

    @Test
    public void offset_known_day() throws Exception {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        Calendar known = Calendar.getInstance();
        known.setTime(df.parse(" 7/4/2018"));
        known.set(Calendar.HOUR_OF_DAY, 12);
        SolarTime solarTime = new SolarTime(location, known.getTimeInMillis());
        assertEquals(0, solarTime.earlierThanUtcHours);
        assertEquals(46, solarTime.earlierThanUtcMinutes, 2);
    }

    @Test
    public void getZoneHours_known_day() throws Exception {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        Calendar known = Calendar.getInstance();
        known.setTime(df.parse(" 7/4/2018"));
        known.set(Calendar.HOUR_OF_DAY, 12);
        SolarTime solarTime = new SolarTime(location, known.getTimeInMillis());
        assertEquals(11, solarTime.getZoneHours(TimeZone.getTimeZone("UTC"), 12, 0));
    }

    @Test
    public void getZoneHours_wraparound() throws Exception {
        SolarTime solarTime = new SolarTime(0, -31);
        assertEquals(13, solarTime.getZoneHours(TimeZone.getTimeZone("UTC"), 12, 55));
    }


    @Test
    public void getZoneMinutes_known_day() throws Exception {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        Calendar known = Calendar.getInstance();
        known.setTime(df.parse(" 7/4/2018"));
        known.set(Calendar.HOUR_OF_DAY, 12);
        SolarTime solarTime = new SolarTime(location, known.getTimeInMillis());
        assertEquals(14, solarTime.getZoneMinutes(TimeZone.getTimeZone("UTC"), 0), 2);
    }

    // if needed, implement these tests
//
////
////
////
////    @Test
////    public void getSolarHours_same_day() {
////        Location location = new MockLocation();
////        location.setLongitude(longitude);
////        location.setLatitude(latitude);
////        SolarTime solarTime = new SolarTime(location);
////        assertEquals(11, solarTime.getZoneHours());
////    }
//
//    // codify result of http://api.usno.navy.mil/rstt/oneday?date=7/4/2018&coords=41.89N,12.48E&tz=1
//    @Test
//    public void solar_transit_fix_in_zone_time() {
//        Location location = MockLocation.fromLatLon(latitude, longitude);
//        SolarTime solarTime = new SolarTime(location);
//        // 7/4/2018
//        Calendar cZone = Calendar.getInstance(solarTime.asTimezone());
//        cZone.set(Calendar.HOUR_OF_DAY, 12);
//        cZone.set(Calendar.MINUTE, 0);
//        cZone.set(Calendar.DAY_OF_MONTH, 4);
//        cZone.set(Calendar.MONTH, Calendar.JULY);
//        cZone.set(Calendar.YEAR, 2018);
//        long orig = cZone.getTimeInMillis();
//        cZone.setTimeZone(TimeZone.getTimeZone("GMT"));
//        long gmt = cZone.getTimeInMillis();
//        assertEquals(orig, gmt);
//
//        assertEquals("11", cZone.getTime().toGMTString()); // result for U - tz
//        assertEquals(11, cZone.get(Calendar.HOUR_OF_DAY)); // result for U - tz
//        assertEquals(14, cZone.get(Calendar.MINUTE), 5);
//
//    }
//
//    @Test
//    public void fixed_date_as_solar() {
//        Location location = MockLocation.fromLatLon(latitude, longitude);
//        SolarTime solarTime = new SolarTime(location);
//        Calendar cZone = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//        cZone.set(Calendar.HOUR_OF_DAY, 12);
//        cZone.set(Calendar.MINUTE, 0);
//        Calendar cSolar = Calendar.getInstance(solarTime.asTimezone());
//        //c.setTime(cSolar.getTime());
//        assertEquals(11, cSolar.get(Calendar.HOUR_OF_DAY)); // result for U - tz
//        assertEquals(14, cSolar.get(Calendar.MINUTE), 5);
//    }
}
