package com.gitlab.kreikenbaum.suntime.data;

import android.location.Location;

public class MockLocation extends Location {
    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    private double latitude;
    private double longitude;

    public MockLocation() {
        super("mock");
    }

    public static MockLocation fromLatLon(double latitude, double longitude) {
        MockLocation out = new MockLocation();
        out.latitude = latitude;
        out.longitude = longitude;
        return out;
    }

    @Override
    public String toString() {
        return "MockLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
