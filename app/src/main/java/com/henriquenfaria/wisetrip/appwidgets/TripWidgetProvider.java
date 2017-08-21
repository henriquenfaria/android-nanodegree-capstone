package com.henriquenfaria.wisetrip.appwidgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import timber.log.Timber;

public class TripWidgetProvider extends AppWidgetProvider {
    // log tag
    private static final String TAG = "TripWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("onUpdate");

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            String tripId = Utils.getStringFromSharedPrefs(context,
                    Constants.Preference.PREFERENCE_WIDGET_TRIP_ID_PREFIX + appWidgetId, "");
            updateAppWidget(context, appWidgetManager, appWidgetId, tripId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Timber.d("onDeleted");
        // When the user deletes the widget, delete the preference associated with it.
        for (int i = 0; i < appWidgetIds.length; i++) {
            Utils.deleteSharedPrefs(context, Constants.Preference.PREFERENCE_WIDGET_TRIP_ID_PREFIX
                    + appWidgetIds[i], true);
        }
    }

    // TODO: Remove unused method
    @Override
    public void onEnabled(Context context) {
        Timber.d("onEnabled");
        // When the first widget is created, register for the TIMEZONE_CHANGED and TIME_CHANGED
        // broadcasts.  We don't want to be listening for these if nobody has our widget active.
        // This setting is sticky across reboots, but that doesn't matter, because this will
        // be called after boot if there is a widget instance for this provider.
      /* PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName("io.appium.android.apis", ".appwidget.ExampleBroadcastReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);*/
    }

    // TODO: Remove unused method
    @Override
    public void onDisabled(Context context) {
        // When the first widget is created, stop listening for the TIMEZONE_CHANGED and
        // TIME_CHANGED broadcasts.
        Timber.d("onDisabled");
       /* PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName("io.appium.android.apis", ".appwidget.ExampleBroadcastReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);*/
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String tripId) {
        Timber.d("updateAppWidget appWidgetId=" + appWidgetId + " tripId=" + tripId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.text, tripId);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
