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
    private final static Location fallbackLocation;
    private static final String LOG_TAG = LocationCache.class.getSimpleName();
    static {
        fallbackLocation = new Location("fallback");  // Berlin
        fallbackLocation.setLatitude(52.53777);
        fallbackLocation.setLongitude(13.6177);
    }

    private static LocationCache INSTANCE;

    private Location location;


    private LocationCache(Context context) {
        Cursor cursor = context.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI_LAST, null, null, null, null);
        location = new Location("stored");
        try {
            cursor.moveToFirst();
            location.setLatitude(cursor.getDouble(cursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_LATITUDE)));
            location.setLongitude(cursor.getDouble(cursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_LONGITUDE)));
            cursor.close();
        } catch (CursorIndexOutOfBoundsException except) {
            Log.w(LOG_TAG, "no location available, falling back to Berlin");
            location = fallbackLocation;
            new LoadGeoIp().execute();
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

    /** needs an AsyncTask, might as well use it to load location data */
    class LoadGeoIp extends AsyncTask<Void, Void, Void> {
        public static final String API_URL = "https://ipapi.co/json";

        @Override
        protected Void doInBackground(Void... voids) {
            URL url;
            try {
                url = new URL(API_URL);
            } catch (MalformedURLException e) {
                throw new RuntimeException("url buggy: " + API_URL); // crash early
            }
            try {
                JSONObject jsonObject = new JSONObject(downloadUrl(url));
                if (LocationCache.this.location.getProvider().equals("fallback")) {
                    Location newLocation = new Location("geoip");
                    newLocation.setLatitude(jsonObject.getDouble("latitude"));
                    newLocation.setLongitude(jsonObject.getDouble("longitude"));
                    LocationCache.this.location = newLocation;
                    Log.i(LOG_TAG, "new location: " + newLocation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private String downloadUrl(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
    }
}
