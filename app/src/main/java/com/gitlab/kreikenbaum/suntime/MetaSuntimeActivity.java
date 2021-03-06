package com.gitlab.kreikenbaum.suntime;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;


import com.gitlab.kreikenbaum.suntime.data.LocationCache;
import com.gitlab.kreikenbaum.suntime.data.SolarTime;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public abstract class MetaSuntimeActivity extends MetaAllActivity {
    protected static final String LOG_TAG = "SuntimeActivity";
    protected static final int LOC_CODE = 1344;

    protected CoordinatorLayout layout;
    protected TextClock sunTime;
    protected TextView sunknown;
    protected SolarTime solarTime;
    protected LocationCache locationCache;

    /** sets up ui elements, onClick, location and suntime */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sun_time);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationCache = LocationCache.getInstance(this);

        layout = findViewById(R.id.layout_sun_time);
        sunTime = findViewById(R.id.tc_suntime);
        sunknown = findViewById(R.id.tv_suntime);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "location: " + locationCache.getLocation());
                if ( locationCache.getLocation() == null ) {
                    Snackbar.make(layout, R.string.alarm_unknown_location,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(MetaSuntimeActivity.this, SunWakeupActivity.class));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        assertLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ( locationCache.getLocation() != null ) {
            updateUi();
        }
    }

    /** asserts location permissions, starts location service */
    protected abstract void assertLocation();

    protected void updateUi() {
        if ( solarTime == null ) {
            solarTime = new SolarTime(locationCache.getLocation());
        }
        sunknown.setVisibility(View.GONE);
        sunTime.setTimeZone(solarTime.toTimezoneString());
        sunTime.setVisibility(View.VISIBLE);
    }

    // https://stackoverflow.com/a/22209857/1587329
    protected void updateWidget() {
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), SunTimeWidget.class));
        SunTimeWidget myWidget = new SunTimeWidget();
        myWidget.onUpdate(this, AppWidgetManager.getInstance(this),ids);

    }
}
