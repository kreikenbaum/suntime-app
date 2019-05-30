package com.gitlab.kreikenbaum.suntime.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gitlab.kreikenbaum.suntime.data.Alarm;


public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (Alarm.getInstance(context).isValid()) {
                AlarmController.restartAlarm(context);
            }
        }
    }
}
