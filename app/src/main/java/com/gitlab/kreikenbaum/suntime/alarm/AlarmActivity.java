package com.gitlab.kreikenbaum.suntime.alarm;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.gitlab.kreikenbaum.suntime.R;

public class AlarmActivity extends Activity {
    private static long ALARM_DURATION = 10 * DateUtils.HOUR_IN_MILLIS;

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AlarmActivity");
        mWakeLock.acquire(ALARM_DURATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // fill status bar with a theme dark color on post-Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_alarm);
    }

    protected void onResume() {
        super.onResume();
        if ( ! mWakeLock.isHeld() ) {
            mWakeLock.acquire(ALARM_DURATION);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        stopAlarm(getCurrentFocus());
        super.onUserLeaveHint();
    }

    @Override
    public void onBackPressed() {
        stopAlarm(getCurrentFocus());
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWakeLock.release();
    }

    public void stopAlarm(View view) {
        AlarmController.dismissAlarm(this);
        AlarmController.restartAlarm(this);
        finish();
    }
}
