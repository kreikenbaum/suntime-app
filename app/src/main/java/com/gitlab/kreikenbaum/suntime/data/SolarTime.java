package com.gitlab.kreikenbaum.suntime.data;

import android.location.Location;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

/** helper for time based on position of sun at location */
public class SolarTime {
    private static final String LOG_TAG = SolarTime.class.getSimpleName();

    private double latitude;
    private double longitude;

    private int earlierThanUtcMillis;
    int earlierThanUtcHours;
    int earlierThanUtcMinutes;

    private long lastTimeMillis;

//    // package access for unit-tests
//    SolarTime(int earlierThanUtcMillis) {
//        this.earlierThanUtcMillis = earlierThanUtcMillis;
//    }
    SolarTime(int earlierThanUtcHours, int earlierThanUtcMinutes) {
        this.earlierThanUtcHours = earlierThanUtcHours;
        this.earlierThanUtcMinutes = earlierThanUtcMinutes;
    }

    public SolarTime(Location location) {
        this(location, System.currentTimeMillis());
    }
    private SolarTime(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public SolarTime(Location location, long timeMillis) {
        this(location.getLatitude(), location.getLongitude());
        computeAt(timeMillis);
    }
    /** compute transit closest to baseMillis (set this at 12:00 of that day to get close) */
    private void computeAt(long baseMillis) {
        this.lastTimeMillis = baseMillis;
        TransitCalculator transit = new TransitCalculator();
        transit.calculateTransit(baseMillis, latitude, longitude);
        Calendar solarNoon = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        solarNoon.setTimeInMillis(transit.transit);
        Calendar utcNoon = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcNoon.setTimeInMillis(baseMillis);
        // need to clean slate, else the minutes etc vary
        cleanSlate(utcNoon);
        // set hour last, as this causes recomputation
        utcNoon.set(Calendar.HOUR_OF_DAY, 12);
        this.earlierThanUtcMillis = (int)(utcNoon.getTimeInMillis() - solarNoon.getTimeInMillis());   // solar + earlier == utc
//        Log.i(LOG_TAG, String.valueOf(this.earlierThanUtcMillis));
//        Pair<Integer, Integer> pair = toHoursMinutes(this.earlierThanUtcMillis);
//        this.earlierThanUtcHours = pair.first;
//        this.earlierThanUtcMinutes = pair.second;
        this.earlierThanUtcHours = this.earlierThanUtcMillis / (int) DateUtils.HOUR_IN_MILLIS;
        //         this.earlierThanUtcHours = (int) Duration.ofMillis(this.earlierThanUtcMillis ).toHours(); // API lvl O
        this.earlierThanUtcMinutes = (
                this.earlierThanUtcMillis - this.earlierThanUtcHours * (int)DateUtils.HOUR_IN_MILLIS)
                / (int)DateUtils.MINUTE_IN_MILLIS;
    }

    private void cleanSlate(Calendar calendar) {
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
// // unused: uses android libs, breaks simple unit tests
//    public Pair<Integer, Integer> toHoursMinutes(int millis) {
//        int hours = millis / (int) DateUtils.HOUR_IN_MILLIS;
//        millis -= hours * (int) DateUtils.HOUR_IN_MILLIS;
//        int minutes = millis / (int) DateUtils.MINUTE_IN_MILLIS;
//        return new Pair<>(hours, minutes);
//    }
//

    /** @return calendar with correct offset, and time of last evaluation */
    public Calendar getCalendar() {
        Calendar c = Calendar.getInstance(this.asTimezone());
        c.setTimeInMillis(this.lastTimeMillis);
        return c;
    }
    private TimeZone asTimezone() {
        return TimeZone.getTimeZone(toTimezoneString());
    }
    public String toTimezoneString() {
        StringBuilder stringBuilder = new StringBuilder("GMT");
        if (this.earlierThanUtcHours >= 0 && this.earlierThanUtcMinutes >= 0) {
            stringBuilder.append("+");
        } else {
            stringBuilder.append("-");
        }
        stringBuilder.append(String.format("%02d:%02d",
                        Math.abs(earlierThanUtcHours), Math.abs(earlierThanUtcMinutes)));
//        Log.i(LOG_TAG, stringBuilder.toString());
        return stringBuilder.toString();
    }

    /** @see #getZoneHours(TimeZone, int, int) with default <code>TimeZone</code> */
    public int getZoneHours(int hours, int minutes) {
        return getZoneHours(TimeZone.getDefault(), hours, minutes);
    }
    /** @see #getZoneMinutes(TimeZone, int) with default <code>TimeZone</code> */
    public int getZoneMinutes(int minutes) {
        return getZoneMinutes(TimeZone.getDefault(), minutes);
    }
    /**
     * This requires that <code>computeAt</code> has been called for the given date.
     * @param zone The TimeZone to report the hours in
     * @param hours the hours to report
     * @param minutes the minutes to report
     * @return hours of time in timezone zone
     */
    int getZoneHours(TimeZone zone, int hours, int minutes) {
        Calendar cZone = this.getCalendar();
        cleanSlate(cZone);
        cZone.set(Calendar.HOUR_OF_DAY, hours);
        cZone.set(Calendar.MINUTE, minutes);
        cZone.getTimeInMillis(); // recompute calendar (otherwise breaks tests, just try it)
        cZone.setTimeZone(zone);
        return cZone.get(Calendar.HOUR_OF_DAY);
    }
    /** @see #getZoneHours(TimeZone, int, int), get minutes */
    int getZoneMinutes(TimeZone zone, int minutes) {
        Calendar cZone = this.getCalendar();
        cleanSlate(cZone);
        cZone.set(Calendar.MINUTE, minutes);
        cZone.getTimeInMillis(); // recompute calendar (otherwise breaks tests, just try it)
        cZone.setTimeZone(zone);
        return cZone.get(Calendar.MINUTE);
    }
}
