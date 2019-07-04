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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

public class SunTimeActivity extends MetaSuntimeActivity implements
        LostApiClient.ConnectionCallbacks, LocationListener, LoadGeoIpTask.SunLocationListener {

    private LostApiClient lostApiClient;

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
    protected void assertLocation() {
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
                .setInterval(1000)
                .setFastestInterval(100)
                .setSmallestDisplacement(100);

        LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, this);
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
