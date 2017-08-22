package com.henriquenfaria.wisetrip.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
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
import com.henriquenfaria.wisetrip.activities.ExpenseFactoryActivity;
import com.henriquenfaria.wisetrip.activities.MainActivity;
import com.henriquenfaria.wisetrip.activities.TripDetailsActivity;
import com.henriquenfaria.wisetrip.models.AttributionModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.io.File;

import timber.log.Timber;

public class TripWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("onUpdate");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidgets(context, appWidgetManager, new int[]{appWidgetId});
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Timber.d("onDeleted");
        // When the user deletes widgets, delete the preference associated with it.
        removeTripFromSharedPrefs(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        Timber.d("onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        Timber.d("onDisabled");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null) {
            String action = intent.getAction();
            if (TextUtils.equals(Constants.Action.ACTION_APPWIDGET_TRIP_UPDATED, action) ||
                    TextUtils.equals(Constants.Action.ACTION_APPWIDGET_TRIP_DELETED, action)) {

                if (intent.hasExtra(Constants.Extra.EXTRA_TRIP_ID)) {
                    String tripId = intent.getStringExtra(Constants.Extra.EXTRA_TRIP_ID);

                    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                    if (intent.hasExtra(Constants.Extra.EXTRA_APPWIDGET_ID)) {
                        appWidgetId = intent.getIntExtra(Constants.Extra.EXTRA_APPWIDGET_ID,
                                AppWidgetManager.INVALID_APPWIDGET_ID);
                    }

                    ComponentName thisWidget = new ComponentName(context.getApplicationContext(),
                            TripWidgetProvider.class);
                    AppWidgetManager appWidgetManager = AppWidgetManager
                            .getInstance(context.getApplicationContext());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

                    // Remove Trip reference from SharedPrefs
                    if (TextUtils.equals(Constants.Action.ACTION_APPWIDGET_TRIP_DELETED, action)) {
                        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            removeTripFromSharedPrefs(context, new int[]{appWidgetId}, tripId);
                        } else {
                            removeTripFromSharedPrefs(context, appWidgetIds, tripId);
                        }
                    }

                    // Update widgets with new Trip data
                    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        updateAppWidgets(context, appWidgetManager, new int[]{appWidgetId});
                    } else {
                        updateAppWidgets(context, appWidgetManager, appWidgetIds);
                    }
                }
            } else if (TextUtils.equals(Constants.Action.ACTION_APPWIDGET_SIGN_OUT, action)) {
                ComponentName thisWidget = new ComponentName(context.getApplicationContext(),
                        TripWidgetProvider.class);
                AppWidgetManager appWidgetManager = AppWidgetManager
                        .getInstance(context.getApplicationContext());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                signOutAppWidgets(context, appWidgetManager, appWidgetIds);
                removeTripFromSharedPrefs(context, appWidgetIds);
            }
        }
    }

    private void updateAppWidgets(final Context context, final AppWidgetManager appWidgetManager,
                                  final int[] appWidgetIds) {

        for (final int appWidgetId : appWidgetIds) {

            final String tripId = Utils.getStringFromSharedPrefs(context,
                    Constants.Preference.PREFERENCE_WIDGET_TRIP_ID_PREFIX + appWidgetId, "");

            Timber.d("updateAppWidgets appWidgetId=" + appWidgetId + " tripId=" + tripId);

            final RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

            final PendingIntent configPendingIntent = getConfigPendingIntent(context,
                    appWidgetId);
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
                        views.setViewVisibility(R.id.select_trip, View.GONE);
                        views.setViewVisibility(R.id.trip_title, View.VISIBLE);
                        views.setViewVisibility(R.id.trip_date, View.VISIBLE);
                        views.setViewVisibility(R.id.trip_photo, View.VISIBLE);

                        views.setTextViewText(R.id.trip_title, trip.getTitle());
                        views.setTextViewText(R.id.trip_date,
                                Utils.getFormattedStartEndShortTripDateText(trip.getStartDate(),
                                        trip.getEndDate()));
                        views.setOnClickPendingIntent(R.id.widget_container,
                                getAddExpensePendingIntent(context, trip, appWidgetId));
                        loadTripPhotoData(context, appWidgetManager, views, tripId,
                                appWidgetId);

                    } else {
                        views.setViewVisibility(R.id.select_trip, View.VISIBLE);
                        views.setTextViewText(R.id.select_trip,
                                context.getString(R.string.select_trip_inside_widget_settings));
                        views.setViewVisibility(R.id.trip_title, View.GONE);
                        views.setViewVisibility(R.id.trip_date, View.GONE);
                        views.setViewVisibility(R.id.trip_photo, View.GONE);

                        views.setOnClickPendingIntent(R.id.widget_container,
                                configPendingIntent);
                        loadTripPhotoData(context, appWidgetManager, views, tripId,
                                appWidgetId);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());
                    views.setViewVisibility(R.id.select_trip, View.VISIBLE);
                    views.setTextViewText(R.id.select_trip,
                            context.getString(R.string.select_trip_inside_widget_settings));
                    views.setViewVisibility(R.id.trip_title, View.GONE);
                    views.setViewVisibility(R.id.trip_date, View.GONE);
                    views.setViewVisibility(R.id.trip_photo, View.GONE);

                    views.setOnClickPendingIntent(R.id.widget_container, configPendingIntent);
                    loadTripPhotoData(context, appWidgetManager, views, tripId, appWidgetId);
                }
            });
        }
    }

    private void signOutAppWidgets(Context context, AppWidgetManager appWidgetManager,
                                   int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            Timber.d("signOutAppWidgets appWidgetId=" + appWidgetId);

            final RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

            views.setViewVisibility(R.id.select_trip, View.VISIBLE);
            views.setTextViewText(R.id.select_trip, context.getString(R.string.not_signed_in));
            views.setViewVisibility(R.id.trip_title, View.GONE);
            views.setViewVisibility(R.id.trip_date, View.GONE);
            views.setViewVisibility(R.id.trip_photo, View.GONE);
            views.setViewVisibility(R.id.attribution_prefix, View.GONE);
            views.setViewVisibility(R.id.attribution_content, View.GONE);

            final PendingIntent configPendingIntent = getConfigPendingIntent(context, appWidgetId);
            views.setOnClickPendingIntent(R.id.settings_button, configPendingIntent);

            final PendingIntent appPendingIntent = getAppPendingIntent(context, appWidgetId);
            views.setOnClickPendingIntent(R.id.widget_container, appPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private PendingIntent getAddExpensePendingIntent(Context context, TripModel trip,
                                                     int appWidgetId) {

        Intent tripDetailsActivityIntent = new Intent(context, TripDetailsActivity.class);
        tripDetailsActivityIntent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) trip);
        Intent addExpense = new Intent(context, ExpenseFactoryActivity.class);
        addExpense.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) trip);

        PendingIntent pendingIntent =
                TaskStackBuilder.create(context)
                        .addNextIntentWithParentStack(tripDetailsActivityIntent)
                        .addNextIntent(addExpense)
                        .getPendingIntent(appWidgetId, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent getConfigPendingIntent(Context context, int appWidgetId) {
        final Intent configIntent = new Intent(context, TripWidgetConfigurationActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        final PendingIntent configPendingIntent = PendingIntent.getActivity(context,
                appWidgetId, configIntent, 0);
        return configPendingIntent;
    }

    private PendingIntent getAppPendingIntent(Context context, int appWidgetId) {
        final Intent configIntent = new Intent(context, MainActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        final PendingIntent configPendingIntent = PendingIntent.getActivity(context,
                appWidgetId, configIntent, 0);
        return configPendingIntent;
    }

    private void loadTripPhotoData(final Context context,
                                   final AppWidgetManager appWidgetManager,
                                   final RemoteViews views, final String tripId,
                                   final int appWidgetId) {
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
                .signature(new ObjectKey(photoFile.lastModified()))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.trip_photo_default)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Bitmap> target,
                                                boolean isFirstResource) {
                        displayPhotoAttribution(context, appWidgetManager, appWidgetId,
                                views, tripId, false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model,
                                                   Target<Bitmap> target, DataSource
                                                           dataSource, boolean
                                                           isFirstResource) {
                        displayPhotoAttribution(context, appWidgetManager, appWidgetId,
                                views, tripId, true);
                        return false;
                    }
                })
                .into(appWidgetTarget);
    }

    private void displayPhotoAttribution(final Context context,
                                         final AppWidgetManager appWidgetManager,
                                         final int appWidgetId,
                                         final RemoteViews views,
                                         final String tripId, boolean shouldDisplay) {

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
                            Intent attrIntent = new Intent(Intent.ACTION_VIEW, Uri.parse
                                    (extractedUrl));
                            attrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            PendingIntent configPendingIntent = PendingIntent.getActivity(context,
                                    appWidgetId, attrIntent, 0);
                            views.setOnClickPendingIntent(R.id.attribution_content,
                                    configPendingIntent);
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

    private void removeTripFromSharedPrefs(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Utils.deleteSharedPrefs(context, Constants.Preference.PREFERENCE_WIDGET_TRIP_ID_PREFIX
                    + appWidgetId, true);
        }
    }

    private void removeTripFromSharedPrefs(Context context, int[] appWidgetIds, String tripId) {
        for (int appWidgetId : appWidgetIds) {
            if (TextUtils.equals(Utils.getStringFromSharedPrefs(context,
                    Constants.Preference.PREFERENCE_WIDGET_TRIP_ID_PREFIX + appWidgetId, null),
                    tripId)) {
                Utils.deleteSharedPrefs(context,
                        Constants.Preference.PREFERENCE_WIDGET_TRIP_ID_PREFIX + appWidgetId, true);
            }
        }
    }
}
