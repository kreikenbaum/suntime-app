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


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.gitlab.kreikenbaum.suntime.data.LocationCache;
import com.gitlab.kreikenbaum.suntime.data.SolarTime;

public class SunTimeActivity extends MetaSuntimeActivity
        implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleApiClient apiClient;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode!=LOC_CODE) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            apiClient.connect();
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            // level 2: check if denied before, tell explanation
            // level 3: option to set location by hand?
            finish();
        }
    }

    // as of https://stackoverflow.com/a/29815513/1587329
    @Override
    protected void onStop() {
        if (apiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
            apiClient.unregisterConnectionCallbacks(this);
            apiClient.disconnect();
        }
        super.onStop();
    }

    /** asserts that some location is available */
    protected void assertLocation() {
        apiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addApi(LocationServices.API)
            .build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            apiClient.connect();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_CODE);
        }
    }


    @SuppressLint("MissingPermission")
    @Override public void onConnected(Bundle bundle) {
        // Client is connected and ready to for use
        onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(apiClient));
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(1000 * 60 * 15)
                .setInterval(1000)
                .setFastestInterval(100)
                .setSmallestDisplacement(100);

        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
    }


    // https://stackoverflow.com/a/22209857/1587329
    private void updateWidget() {
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), SunTimeWidget.class));
        SunTimeWidget myWidget = new SunTimeWidget();
        myWidget.onUpdate(this, AppWidgetManager.getInstance(this),ids);
    }


    @Override
    public void onConnectionSuspended(int sth) {
        // pass
    }

    @Override
    public void onLocationChanged(Location location) {
        if ( location == null ) {
            Log.w(LOG_TAG, "location is null");
            return;
        }
        locationCache.setLocation(this, location);
        solarTime = new SolarTime(locationCache.getLocation());
        updateUi();
        updateWidget();
    }

}
