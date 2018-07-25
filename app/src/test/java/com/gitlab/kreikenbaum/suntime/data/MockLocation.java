/* Copyright (C) 2018 gitlab.com/kreikenbaum
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
