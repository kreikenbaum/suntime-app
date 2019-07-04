package com.gitlab.kreikenbaum.suntime;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** load location via AsyncTask */
public class LoadGeoIpTask extends AsyncTask<LoadGeoIpTask.SunLocationListener, Void, Location> {
    private static final String API_URL = "https://ipapi.co/json";
    private static final String LOG_TAG = LoadGeoIpTask.class.getSimpleName();
    private SunLocationListener listener;

    @Override @Nullable
    protected Location doInBackground(SunLocationListener... listeners) {
        listener = listeners[0];
        URL url;
        try {
            url = new URL(API_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException("url buggy: " + API_URL); // crash early
        }
        Location newLocation = null;
        try {
            JSONObject jsonObject = new JSONObject(downloadUrl(url));
            newLocation = new Location("geoip");
            newLocation.setLatitude(jsonObject.getDouble("latitude"));
            newLocation.setLongitude(jsonObject.getDouble("longitude"));
            Log.i(LOG_TAG, "location from IP: " + newLocation);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newLocation;
    }

    @Override
    protected void onPostExecute(Location location) {
        listener.onLocationChanged(location);
    }

    public interface SunLocationListener {
        void onLocationChanged(Location location);
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


