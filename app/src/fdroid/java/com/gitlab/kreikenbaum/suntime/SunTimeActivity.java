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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;
import android.widget.TextClock;
import android.widget.Toast;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.gitlab.kreikenbaum.suntime.data.LocationCache;
import com.gitlab.kreikenbaum.suntime.data.SolarTime;

public class SunTimeActivity extends AppCompatActivity
        implements LostApiClient.ConnectionCallbacks, LocationListener {
    private static final String LOG_TAG = SunTimeActivity.class.getSimpleName();
    private static final int LOC_CODE = 1344;

    private TextClock sunTime;
    private SolarTime solarTime;
    private LostApiClient lostApiClient;
    private LocationCache locationCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sun_time);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SunTimeActivity.this, SunWakeupActivity.class));
            }
        });
        sunTime = findViewById(R.id.tc_suntime);
        locationCache = LocationCache.getInstance(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sun_time, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void updateWidget() {  //https://stackoverflow.com/questions/2929393
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.sun_time_widget);
        ComponentName thisWidget = new ComponentName(this, SunTimeWidget.class);
        AppWidgetManager.getInstance(this).updateAppWidget( thisWidget, remoteViews );
    }

    @Override
    public void onConnectionSuspended() {

    }

    @Override
    public void onLocationChanged(Location location) {
        locationCache.setLocation(this, location);
        solarTime = new SolarTime(locationCache.getLocation());
        sunTime.setTimeZone(solarTime.toTimezoneString());
        updateWidget();
    }
}
