package com.gitlab.kreikenbaum.suntime;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.widget.TextClock;
import android.widget.TextView;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_meta_suntime);
    }
}
