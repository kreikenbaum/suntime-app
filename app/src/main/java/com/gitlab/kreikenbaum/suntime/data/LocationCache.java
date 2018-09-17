package com.gitlab.kreikenbaum.suntime.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.gitlab.kreikenbaum.suntime.provider.LocationContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationCache {
    private static final String LOG_TAG = LocationCache.class.getSimpleName();


    private static LocationCache INSTANCE;

    private Location location;


    private LocationCache(Context context) {
        try (Cursor cursor = context.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI_LAST, null, null, null, null)) {
            cursor.moveToFirst();
            Location tmp = new Location("stored");
            tmp.setLatitude(cursor.getDouble(cursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_LATITUDE)));
            tmp.setLongitude(cursor.getDouble(cursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_LONGITUDE)));
            this.location = tmp;
        } catch (CursorIndexOutOfBoundsException except) {
            Log.w(LOG_TAG, "no location available, trying IP address");
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Context context, Location location) {
        if (location == null) {
            Log.w(LOG_TAG, "setLocation: location is null");
            return;
        }
        this.location = location;
        context.getContentResolver().delete(LocationContract.LocationEntry.CONTENT_URI, null, null);
        ContentValues cv = new ContentValues();
        cv.put(LocationContract.LocationEntry.COLUMN_LATITUDE, location.getLatitude());
        cv.put(LocationContract.LocationEntry.COLUMN_LONGITUDE, location.getLongitude());
        context.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, cv);
    }

    public static LocationCache getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LocationCache.class) {
                INSTANCE = new LocationCache(context);
            }
        }
        return INSTANCE;
    }
}
