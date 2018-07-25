package com.gitlab.kreikenbaum.suntime.alarm;

// all of this directory's code is from https://github.com/trikita/talalarmo/tree/master/src/main/java/trikita/talalarmo/alarm

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.gitlab.kreikenbaum.suntime.SunWakeupActivity;
import com.gitlab.kreikenbaum.suntime.data.Alarm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmController {
    static void restartAlarm(Context context) {
        Calendar c = Alarm.getInstance(context).getNextOccurrence();
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {        // KITKAT and later
                am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
            }
            intent = new Intent("android.intent.action.ALARM_CHANGED");
            intent.putExtra("alarmSet", true);
            context.sendBroadcast(intent);
            SimpleDateFormat fmt = new SimpleDateFormat("E HH:mm");
            Settings.System.putString(context.getContentResolver(),
                    Settings.System.NEXT_ALARM_FORMATTED,
                    fmt.format(c.getTime()));
        } else {
            Intent showIntent = new Intent(context, AlarmActivity.class);
            PendingIntent showOperation = PendingIntent.getActivity(context, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), showOperation);
            am.setAlarmClock(alarmClockInfo, sender);
            Log.i("alarmcontroller", "time set for " + new Date(am.getNextAlarmClock().getTriggerTime()));
        }
    }

    private void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent("android.intent.action.ALARM_CHANGED");
            intent.putExtra("alarmSet", false);
            context.sendBroadcast(intent);
            Settings.System.putString(context.getContentResolver(),
                    Settings.System.NEXT_ALARM_FORMATTED, "");
        }
    }

    static void wakeupAlarm(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl =
                pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "AlarmReceiver");
        wl.acquire(5000);
        context.startService(new Intent(context, AlarmService.class));
    }

    static void dismissAlarm(Context context) {
        context.stopService(new Intent(context, AlarmService.class));
    }
}
