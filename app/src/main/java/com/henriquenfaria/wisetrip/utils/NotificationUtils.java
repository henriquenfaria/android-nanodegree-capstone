package com.henriquenfaria.wisetrip.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.activities.MainActivity;
import com.henriquenfaria.wisetrip.activities.TripDetailsActivity;
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.models.TripModel;

/* Utility class with static helper methods for notifications */
public class NotificationUtils {

    public static final int BUDGET_LIMIT_EXCEEDED_NOTIFICATION_START_ID = 1;

    public static void notifyBudgetLimitExceeded(Context context, BudgetModel budget,
                                                 TripModel trip) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context);

        Intent tripDetails = new Intent(context, TripDetailsActivity.class);
        tripDetails.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) trip);
        tripDetails.putExtra(Constants.Extra.EXTRA_TRIP_DETAILS_TAB_INDEX,
                TripDetailsActivity.TAB_BUDGETS_POSITION);
        Intent mainActivityIntent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent =
                TaskStackBuilder.create(context)
                        .addNextIntentWithParentStack(mainActivityIntent)
                        .addNextIntent(tripDetails)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(budget.getTitle())
                .setContentText(context.getString(R.string.budget_limit_exceeded))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Do not replace previous budget notification
        int nextNotificationId =  Utils.getIntFromSharedPrefs(context,
                Constants.Preference.PREFERENCE_LAST_BUDGET_NOTIFICATION_ID,
                BUDGET_LIMIT_EXCEEDED_NOTIFICATION_START_ID) + 1;

        Utils.saveIntToSharedPrefs(context,
                Constants.Preference.PREFERENCE_LAST_BUDGET_NOTIFICATION_ID,
                nextNotificationId, true);

        mNotificationManager.notify(nextNotificationId, mBuilder.build());
    }
}
