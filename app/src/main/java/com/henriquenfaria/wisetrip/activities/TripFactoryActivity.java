package com.henriquenfaria.wisetrip.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.TripFactoryFragment;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;

public class TripFactoryActivity extends AppCompatActivity
        implements TripFactoryFragment.OnTripFactoryListener {

    private static final String LOG_TAG = TripFactoryActivity.class.getSimpleName();

    private static final String TAG_TRIP_FACTORY_FRAGMENT = "tag_trip_factory_fragment";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserTripReference;
    private FirebaseUser mCurrentUser;
    private Trip mTrip;
    private TripFactoryFragment mTripFactoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_factory);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserTripReference = mFirebaseDatabase.getReference()
                .child("user-trips")
                .child(mCurrentUser.getUid());

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Constants.Extras.EXTRA_TRIP)) {
                // Trip already exists
                mTrip = intent.getParcelableExtra(Constants.Extras.EXTRA_TRIP);
                mTripFactoryFragment = TripFactoryFragment.newInstance(mTrip);

            } else {
                // New trip
                mTripFactoryFragment = TripFactoryFragment.newInstance();
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
        getSupportActionBar().setTitle(newTitle);
    }

    @Override
    public void saveTrip(Trip trip, boolean isEditMode) {
        //TODO: need to verify "isEditMode" to update a trip instead of adding a new one
        if (isEditMode) {
            // Update existing trip value
        } else {
            mUserTripReference.push().setValue(trip);
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
