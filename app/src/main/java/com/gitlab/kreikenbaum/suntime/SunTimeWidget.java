package com.gitlab.kreikenbaum.suntime;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.gitlab.kreikenbaum.suntime.data.LocationCache;
import com.gitlab.kreikenbaum.suntime.data.SolarTime;

/**
 * Implementation of App Widget functionality.
 */
public class SunTimeWidget extends AppWidgetProvider {
    private static final String LOG_TAG = SunTimeWidget.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d(LOG_TAG, "update widget " + appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sun_time_widget);
        Location location = LocationCache.getInstance(context).getLocation();
        if ( location != null ) {
            SolarTime solarTime = new SolarTime(location);
            views.setViewVisibility(R.id.tv_sunknown, View.GONE);
            views.setViewVisibility(R.id.widget_1_hours, View.VISIBLE);
            views.setViewVisibility(R.id.widget_1_minutes, View.VISIBLE);
            views.setString(R.id.widget_1_hours, "setTimeZone", solarTime.toTimezoneString());
            views.setString(R.id.widget_1_minutes, "setTimeZone", solarTime.toTimezoneString());
            Log.d(LOG_TAG, "with timeZone " + solarTime.toTimezoneString());
        }

        Intent startSunActivity = new Intent(context, SunTimeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, startSunActivity, 0);
        views.setOnClickPendingIntent(R.id.widget_1, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

