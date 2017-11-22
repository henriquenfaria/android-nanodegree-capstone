package com.henriquenfaria.wisetrip.appwidgets;


import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.activities.FirebaseBaseActivity;
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.holders.TripHolder;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;
import com.henriquenfaria.wisetrip.views.SizeAwareRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Configuration Activity for Trip appwidget.
 * User can select a trip to be displayed in the homescreen widget.
 */
public class TripWidgetConfigurationActivity extends FirebaseBaseActivity {

    @BindView(R.id.trip_list)
    protected SizeAwareRecyclerView mTripListRecyclerView;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Query mTripsQuery;
    private FirebaseRecyclerAdapter<TripModel, TripHolder> mAdapter;
    private FirebaseRecyclerOptions<TripModel> mOptions;

    public TripWidgetConfigurationActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Not signed in, finish Configuration
        if (mCurrentUser == null) {
            Toast.makeText(this, R.string.must_sign_in_first, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set the view layout resource to use.
        setContentView(R.layout.activity_expenses_widget_configuration);
        ButterKnife.bind(this);

        mTripsQuery = mFirebaseDatabase.getReference()
                .child(FirebaseDbContract.Trips.PATH_TRIPS)
                .child(mCurrentUser.getUid())
                .orderByChild(FirebaseDbContract.Trips.PATH_TITLE);

        mOptions = new FirebaseRecyclerOptions.Builder<TripModel>()
                .setQuery(mTripsQuery, TripModel.class)
                .build();


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
        mTripListRecyclerView.setEmptyView(findViewById(R.id.empty_view));
        mTripListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTripListRecyclerView.setAdapter(mAdapter);
    }

    private FirebaseRecyclerAdapter<TripModel, TripHolder> getAdapter() {
        return new FirebaseRecyclerAdapter<TripModel, TripHolder>(mOptions) {
            @Override
            public TripHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(TripWidgetConfigurationActivity.this)
                        .inflate(R.layout.trip_item, parent, false);
                return new TripHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(TripHolder holder, int position, final TripModel trip) {
                if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                    holder.setTripTitle(trip.getTitle());
                    holder.setTripDate(Utils.getFormattedStartEndTripDateText(trip
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

                            Utils.updateAppWidget(TripWidgetConfigurationActivity.this,
                                    trip.getId(), mAppWidgetId, false);

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
    protected void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }
}
