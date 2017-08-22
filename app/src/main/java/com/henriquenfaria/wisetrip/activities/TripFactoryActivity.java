package com.henriquenfaria.wisetrip.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.fragments.TripFactoryFragment;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;

public class TripFactoryActivity extends AppCompatActivity
        implements TripFactoryFragment.OnTripFactoryListener {

    private static final String TAG_TRIP_FACTORY_FRAGMENT = "tag_trip_factory_fragment";

    private TripFactoryFragment mTripFactoryFragment;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_factory);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // TripModel already exists
                TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                mTripFactoryFragment = TripFactoryFragment.newInstance(trip);

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
            final DatabaseReference tripReference
                    = mFirebaseDatabase.getReference().child(FirebaseDbContract.Trips.PATH_TRIPS)
                    .child(mCurrentUser.getUid());

            if (isEditMode && !TextUtils.isEmpty(trip.getId())) {
                DatabaseReference databaseReference = tripReference.child(trip.getId());
                databaseReference.setValue(trip);
            } else if (!isEditMode) {
                DatabaseReference databaseReference = tripReference.push();
                trip.setId(databaseReference.getKey());
                databaseReference.setValue(trip);
            } else {
                Toast.makeText(this, getString(R.string.trip_updated_error),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.trip_updated_error),
                    Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public void deleteTrip(TripModel trip) {
        final DatabaseReference tripReference
                = mFirebaseDatabase.getReference().child(FirebaseDbContract.Trips.PATH_TRIPS)
                .child(mCurrentUser.getUid());

        if (trip != null) {
            tripReference.child(trip.getId()).removeValue();
            removeOtherTripData(trip);
        } else {
            Toast.makeText(this, getString(R.string.trip_updated_error),
                    Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    // Remove trip attributions, expenses, budgets and places
    private void removeOtherTripData(TripModel trip) {
        // Remove Trip attributions
        final DatabaseReference attributionsReference = mFirebaseDatabase.getReference()
                .child(FirebaseDbContract.Attributions.PATH_ATTRIBUTIONS)
                .child(mCurrentUser.getUid())
                .child(trip.getId());
        attributionsReference.removeValue();

        // Remove Trip expenses
        final DatabaseReference expensesReference = mFirebaseDatabase.getReference()
                .child(FirebaseDbContract.Expenses.PATH_EXPENSES)
                .child(mCurrentUser.getUid())
                .child(trip.getId());
        expensesReference.removeValue();

        // Remove Trip budgets
        final DatabaseReference budgetsReference = mFirebaseDatabase.getReference()
                .child(FirebaseDbContract.Budgets.PATH_BUDGETS)
                .child(mCurrentUser.getUid())
                .child(trip.getId());
        budgetsReference.removeValue();

        // Remove Trip places
        final DatabaseReference placesReference = mFirebaseDatabase.getReference()
                .child(FirebaseDbContract.Places.PATH_PLACES)
                .child(mCurrentUser.getUid())
                .child(trip.getId());
        placesReference.removeValue();
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
