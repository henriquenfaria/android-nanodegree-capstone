package com.henriquenfaria.wisetrip.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.GlideApp;
import com.henriquenfaria.wisetrip.GlideRequest;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.AttributionModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.io.File;

import timber.log.Timber;

public class TripWidgetProvider extends AppWidgetProvider {

    // TODO: Static or not static? Move to not static if using broadcasts
    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId, final String tripId) {
        Timber.d("updateAppWidget appWidgetId=" + appWidgetId + " tripId=" + tripId);

        if (!TextUtils.isEmpty(tripId)) {
            final RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

            // Set widget config Intent
            final Intent configIntent = new Intent(context, TripWidgetConfigurationActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            final PendingIntent configPendingIntent = PendingIntent.getActivity(context,
                    appWidgetId, configIntent, 0);
            views.setOnClickPendingIntent(R.id.settings_button, configPendingIntent);

            // Set Database References and Listeners
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            DatabaseReference tripReference = firebaseDatabase.getReference()
                    .child("trips")
                    .child(currentUser.getUid())
                    .child(tripId);
            tripReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Timber.d("onDataChange");

                    TripModel trip = dataSnapshot.getValue(TripModel.class);
                    if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                        views.setTextViewText(R.id.trip_title, trip.getTitle());
                        views.setViewVisibility(R.id.select_trip, View.GONE);
                        loadTripPhoto(context, appWidgetManager, views, tripId, appWidgetId);
                    } else {
                        views.setViewVisibility(R.id.select_trip, View.VISIBLE);
                        views.setOnClickPendingIntent(R.id.widget_container, configPendingIntent);
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());
                }
            });
        }
    }

    private static void loadTripPhoto(final Context context, final AppWidgetManager appWidgetManager,
                                      final RemoteViews views, final String tripId,
                                      final int appWidgetId) {
        if (!TextUtils.isEmpty(tripId)) {
            ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
            File directoryFile = cw.getDir(Constants.General.DESTINATION_PHOTO_DIR,
                    Context.MODE_PRIVATE);
            final File photoFile = new File(directoryFile, tripId);

            AppWidgetTarget appWidgetTarget =
                    new AppWidgetTarget(context, R.id.trip_photo, views, appWidgetId);

            GlideRequest<Bitmap> glideRequest = GlideApp.with(context).asBitmap();

            glideRequest
                    .load(photoFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_default_traveler_photo)
                    .error(R.drawable.ic_default_traveler_photo)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            displayPhotoAttribution(context, appWidgetManager, appWidgetId, views, tripId, false);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            displayPhotoAttribution(context, appWidgetManager, appWidgetId, views, tripId, true);
                            return false;
                        }
                    })
                    .into(appWidgetTarget);
        }
    }

    private static void displayPhotoAttribution(final Context context,
                                                final AppWidgetManager appWidgetManager,
                                                final int appWidgetId,
                                                final RemoteViews views,
                                                String tripId, boolean shouldDisplay) {

        if (shouldDisplay) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference attributionsReference = firebaseDatabase
                    .getReference()
                    .child("attributions")
                    .child(currentUser.getUid())
                    .child(tripId);

            attributionsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Timber.d("onDataChange");
                    AttributionModel attribution = dataSnapshot.getValue(AttributionModel
                            .class);
                    if (attribution != null && !TextUtils.isEmpty(attribution.getText())) {
                        Spanned result;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            result = Html.fromHtml(attribution.getText(),
                                    Html.FROM_HTML_MODE_LEGACY);
                        } else {
                            result = Html.fromHtml(attribution.getText());
                        }

                        views.setTextViewText(R.id.attribution_content, result);

                        // Set url pending intent
                        String extractedUrl = Utils.getUrlFromHtml(attribution.getText());
                        if (!TextUtils.isEmpty(extractedUrl)) {
                            Intent attrIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(extractedUrl));
                            attrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            PendingIntent configPendingIntent = PendingIntent.getActivity(context,
                                    appWidgetId, attrIntent, 0);
                            views.setOnClickPendingIntent(R.id.attribution_content, configPendingIntent);
                        }

                        views.setViewVisibility(R.id.attribution_prefix, View.VISIBLE);
                        views.setViewVisibility(R.id.attribution_content, View.VISIBLE);

                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            });
        } else {
            views.setViewVisibility(R.id.attribution_prefix, View.GONE);
            views.setViewVisibility(R.id.attribution_content, View.GONE);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

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
}
