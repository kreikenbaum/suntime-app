package com.gitlab.kreikenbaum.suntime;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import com.gitlab.kreikenbaum.suntime.data.LocationCache;
import com.gitlab.kreikenbaum.suntime.data.SolarTime;

public abstract class MetaSuntimeActivity extends AppCompatActivity {
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
        if (locationCache.getLocation() != null) {
            solarTime = new SolarTime(locationCache.getLocation());
        }

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

    /** asserts location permissions, starts location service */
    protected abstract void assertLocation();

    protected void updateUi() {
        sunknown.setVisibility(View.GONE);
        sunTime.setTimeZone(solarTime.toTimezoneString());
        sunTime.setVisibility(View.VISIBLE);
    }
}
