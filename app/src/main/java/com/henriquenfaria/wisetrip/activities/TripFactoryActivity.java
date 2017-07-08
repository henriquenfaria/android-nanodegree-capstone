package com.henriquenfaria.wisetrip.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.TripFactoryFragment;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;

public class TripFactoryActivity extends AppCompatActivity
        implements TripFactoryFragment.OnTripFactoryListener {

    private static final String TAG_TRIP_FACTORY_FRAGMENT = "tag_trip_factory_fragment";

    private TripModel mTrip;
    private TripFactoryFragment mTripFactoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_factory);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // TripModel already exists
                mTrip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                mTripFactoryFragment = TripFactoryFragment.newInstance(mTrip);

            } else {
                // New trip
                mTripFactoryFragment = TripFactoryFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.trip_factory_fragment_container, mTripFactoryFragment,
                            TAG_TRIP_FACTORY_FRAGMENT).commit();
        } else {
            // Fragment already exists, just get it using its TAG
            mTripFactoryFragment = (TripFactoryFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_TRIP_FACTORY_FRAGMENT);
        }
    }


    @Override
    public void changeActionBarTitle(String newTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(newTitle);
        }
    }

    @Override
    public void saveTrip(TripModel trip, boolean isEditMode) {
        if (trip != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) trip);
            setResult(isEditMode ? Constants.Result.RESULT_TRIP_CHANGED
                    : Constants.Result.RESULT_TRIP_ADDED, resultIntent);
        } else {
            setResult(Constants.Result.RESULT_TRIP_ERROR);
        }

        finish();
    }

    @Override
    public void deleteTrip(TripModel trip) {
        if (trip != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) trip);
            setResult(Constants.Result.RESULT_TRIP_REMOVED, resultIntent);
        } else {
            setResult(Constants.Result.RESULT_TRIP_ERROR);
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // To animate transition like back button press
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
