package com.henriquenfaria.wisetrip.appwidgets;


import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.holders.TripHolder;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;
import com.henriquenfaria.wisetrip.views.SizeAwareRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripWidgetConfigurationActivity extends AppCompatActivity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private Query mTripsQuery;
    private FirebaseUser mCurrentUser;
    private FirebaseRecyclerAdapter<TripModel, TripHolder> mAdapter;

    @BindView(R.id.trip_list)
    protected SizeAwareRecyclerView mTripListRecyclerView;

    public TripWidgetConfigurationActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.activity_expenses_widget_configuration);
        ButterKnife.bind(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mTripsQuery = mFirebaseDatabase.getReference()
                .child("trips")
                .child(mCurrentUser.getUid())
                .orderByChild("title");

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        mAdapter = getAdapter();
        mTripListRecyclerView.setEmptyView(ButterKnife.findById(this, R.id.empty_view));
        mTripListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTripListRecyclerView.setAdapter(mAdapter);
    }

    private FirebaseRecyclerAdapter<TripModel, TripHolder> getAdapter() {
        return new FirebaseRecyclerAdapter<TripModel, TripHolder>(
                TripModel.class,
                R.layout.trip_item,
                TripHolder.class,
                mTripsQuery) {
            @Override
            public void populateViewHolder(final TripHolder holder,
                                           final TripModel trip, final int position) {
                if (trip != null && !TextUtils.isEmpty(trip.getId())){
                    holder.setTripTitle(trip.getTitle());
                    holder.setTripDate(Utils.getFormattedFullTripDateText(trip
                            .getStartDate(), trip.getEndDate()));
                    holder.setTripPhoto(trip.getId());
                    holder.hideEdit();
                    holder.setOnTripItemClickListener(new TripHolder.OnTripItemClickListener() {
                        @Override
                        public void onTripItemClick(View view) {
                            // Create a connection between trip id and widget id
                            Utils.saveStringToSharedPrefs(TripWidgetConfigurationActivity.this,
                                    Constants.Preference.PREFERENCE_WIDGET_TRIP_ID_PREFIX
                                            + mAppWidgetId, trip.getId(), true);

                            AppWidgetManager appWidgetManager = AppWidgetManager
                                    .getInstance(TripWidgetConfigurationActivity.this);
                            TripWidgetProvider.updateAppWidget(TripWidgetConfigurationActivity.this,
                                    appWidgetManager, mAppWidgetId, trip.getId());

                            Intent resultValue = new Intent();
                            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                            setResult(RESULT_OK, resultValue);
                            finish();
                        }
                    });
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }
}
