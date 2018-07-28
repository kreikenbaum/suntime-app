package com.gitlab.kreikenbaum.suntime.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gitlab.kreikenbaum.suntime.alarm.AlarmReceiver;
import com.gitlab.kreikenbaum.suntime.SunWakeupActivity;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

// level 2: list of alarms, maybe use room
/** singleton: alarm: hour and minute */
public class Alarm {
    private static final String HOUR = "HOUR";
    private static final String MINUTE = "MINUTE";
    private static final String LOG_TAG = Alarm.class.getSimpleName();

    private static Alarm INSTANCE;

    private int hour;
    private int minute;
    private LocationCache locationCache;


    private Alarm(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        hour = sharedPreferences.getInt(HOUR, -1);
        minute = sharedPreferences.getInt(MINUTE, -1);
        locationCache = LocationCache.getInstance(context);
    }

    public static Alarm getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Alarm.class) {
                INSTANCE = new Alarm(context);
            }
        }
        return INSTANCE;
    }


    public int getSolarHour() {
        if ( this.isValid() ) {
            return hour;
        } else {
            return 9;
        }
    }
    public int getSolarMinute() {
        if ( this.isValid() ) {
            return minute;
        } else {
            return 0;
        }
    }
    public int getZoneHour() {
        SolarTime solarTime = new SolarTime(locationCache.getLocation());
        return solarTime.getZoneHours(getSolarHour(), getSolarMinute());
    }
    public int getZoneMinute() {
        SolarTime solarTime = new SolarTime(locationCache.getLocation());
        return solarTime.getZoneMinutes(getSolarMinute());
    }

    public boolean isValid() {
        return this.hour >= 0 && this.minute >= 0;
    }

    public void setTime(Context context, int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(HOUR, hour);
        editor.putInt(MINUTE, minute);
        editor.apply();
        schedule(context);
    }

    // TODO: should really use DateFormat, but that needs a date, and a timezone,...
    // TODO: test different locales (at least those supported)
    public String toSolarTimeString() {
        return String.format(Locale.getDefault(), "%d:%02d",
                             this.getSolarHour(), this.getSolarMinute());
    }

    public String toZoneTimeString() {
        return String.format(Locale.getDefault(), "%d:%02d",
                             this.getZoneHour(), this.getZoneMinute());
    }

    public Calendar getNextOccurrence() {
        Calendar nextOccurrence = Calendar.getInstance();
        nextOccurrence.set(Calendar.HOUR_OF_DAY, getZoneHour());
        nextOccurrence.set(Calendar.MINUTE, getZoneMinute());
        if ( nextOccurrence.before(Calendar.getInstance()) ) {
            nextOccurrence.add(Calendar.DAY_OF_YEAR, 1);
        }
        return nextOccurrence;
    }

    public void schedule(Context context) {
        schedule(context, getNextOccurrence());
    }
    public void schedule(Context context, int minutesFromNow) {
        Calendar nextOccurrence = Calendar.getInstance();
        nextOccurrence.add(Calendar.MINUTE, minutesFromNow);
        schedule(context, nextOccurrence);
    }
    private void schedule(Context context, Calendar nextOccurrence) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingAlarm = PendingIntent.getBroadcast(
                context, 0, alarmIntent, 0);
        Intent editIntent = new Intent(context, SunWakeupActivity.class);
        PendingIntent pendingEdit = PendingIntent.getActivity(context, 0, editIntent, 0);
        Log.i(LOG_TAG, "next alarm at " + nextOccurrence.getTime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(nextOccurrence.getTimeInMillis(),
                    pendingEdit), pendingAlarm);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, nextOccurrence.getTimeInMillis(), pendingAlarm);
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP, nextOccurrence.getTimeInMillis(), pendingAlarm);
        }
    }
}
