package com.gitlab.kreikenbaum.suntime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.gitlab.kreikenbaum.suntime.data.LocationCache;
import com.gitlab.kreikenbaum.suntime.data.SolarTime;

public class SunTimeActivity extends AppCompatActivity implements
        LostApiClient.ConnectionCallbacks, LocationListener, LoadGeoIpTask.SunLocationListener {
    private static final String LOG_TAG = SunTimeActivity.class.getSimpleName();
    private static final int LOC_CODE = 1344;

    private CoordinatorLayout layout;
    private TextClock sunTime;
    private TextView sunknown;
    private SolarTime solarTime;
    private LostApiClient lostApiClient;
    private LocationCache locationCache;


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
                    startActivity(new Intent(SunTimeActivity.this, SunWakeupActivity.class));
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode!=LOC_CODE) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lostApiClient.connect();
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            finish();
        }
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

    // as of https://stackoverflow.com/a/29815513/1587329
    @Override
    protected void onStop() {
        if (lostApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, this);
            lostApiClient.unregisterConnectionCallbacks(this);
            lostApiClient.disconnect();
        }
        super.onStop();
    }

    /** asserts that some location is available */
    private void assertLocation() {
        lostApiClient = new LostApiClient.Builder(this)
            .addConnectionCallbacks(this).build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            lostApiClient.connect();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_CODE);
        }
    }


    @SuppressLint("MissingPermission")
    @Override public void onConnected() {
        // Client is connected and ready to for use
        onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(lostApiClient));
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(1000 * 60 * 15)
                .setInterval(1000)
                .setFastestInterval(100)
                .setSmallestDisplacement(100);

        LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, this);
    }

    private void updateUi() {
        sunknown.setVisibility(View.GONE);
        sunTime.setTimeZone(solarTime.toTimezoneString());
        sunTime.setVisibility(View.VISIBLE);
    }

    // https://stackoverflow.com/a/22209857/1587329
    private void updateWidget() {
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), SunTimeWidget.class));
        SunTimeWidget myWidget = new SunTimeWidget();
        myWidget.onUpdate(this, AppWidgetManager.getInstance(this),ids);

    }

    @Override
    public void onConnectionSuspended() {

    }

    @Override
    public void onLocationChanged(Location location) {
        if ( location == null ) {
            Log.w(LOG_TAG, "location is null");
            if (locationCache.getLocation() == null) {
                Log.i(LOG_TAG, "ip location needed");
                new LoadGeoIpTask().execute(this);
            }
            return;
        }
        locationCache.setLocation(this, location);
        solarTime = new SolarTime(locationCache.getLocation());
        updateUi();
        updateWidget();
    }
}
