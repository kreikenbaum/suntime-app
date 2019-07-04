package com.gitlab.kreikenbaum.suntime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
        onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(apiClient));
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(100)
                .setSmallestDisplacement(100);

        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
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
