package com.gitlab.kreikenbaum.suntime;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gitlab.kreikenbaum.suntime.data.Alarm;
import com.gitlab.kreikenbaum.suntime.data.LocationCache;
import com.gitlab.kreikenbaum.suntime.data.SolarTime;

import java.util.TimeZone;


public class SunWakeupActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener {
    private CoordinatorLayout layout;
    private TextView sunTime;
    private TextView zoneName;
    private TextView zoneTime;
    private LocationCache locationCache;
    private Alarm alarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sun_wakeup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        layout = findViewById(R.id.main_wakeup_layout);
        sunTime = findViewById(R.id.tv_alarm_time);
        zoneTime = findViewById(R.id.tv_zone_time);
        zoneName = findViewById(R.id.tv_zone_name);
        locationCache = LocationCache.getInstance(this);
        alarm = Alarm.getInstance(this);

        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarm.setTime(SunWakeupActivity.this, -1, -1);
                zoneTime.setText(R.string.wakeup_zonetime_unset);
                sunTime.setText(R.string.wakeup_suntime_unset);
                zoneName.setText(R.string.zone_time);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (alarm.isValid()) {
            showTimes();
        }
    }


    public void selectAlarm(View view) {
        TimePickerDialog dialog = new TimePickerDialog(this, this,
                alarm.getSolarHour(), alarm.getSolarMinute(), DateFormat.is24HourFormat(this));
        dialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        alarm.setTime(this, hourOfDay, minute); // registers alarm
        showTimes();
        Snackbar.make(layout,
                      "Alarm set to " + alarm.toZoneTimeString()
                      //                      + " (" + getResources().getString(R.string.zone_time) + ")",
                      + " (" + TimeZone.getDefault().getID() + ")",
                      Snackbar.LENGTH_SHORT).show();
    }

    private void showTimes() {
        sunTime.setText(alarm.toSolarTimeString());
        zoneName.setText(TimeZone.getDefault().getID());
        zoneTime.setText(alarm.toZoneTimeString());
    }
}
