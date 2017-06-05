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
import com.henriquenfaria.wisetrip.fragments.TripFactoryFragment;
import com.henriquenfaria.wisetrip.models.Destination;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.service.PlacePhotoIntentService;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.util.List;

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
                mTripFactoryFragment = TripFactoryFragment.newInstance(new Trip());
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

        if (isEditMode) {
            // Update existing Trip
            if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                DatabaseReference databaseReference = mUserTripReference.child(trip.getId());
                databaseReference.setValue(trip);
                Toast.makeText(this, getString(R.string.trip_updated_success),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.trip_updated_error),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Creating Trip
            DatabaseReference databaseReference = mUserTripReference.push();
            trip.setId(databaseReference.getKey());
            databaseReference.setValue(trip);
            Toast.makeText(this, getString(R.string.trip_created_success), Toast
                    .LENGTH_SHORT).show();
        }

        //TODO: Temporary code
        Intent placePhotoIntentService = new Intent(this, PlacePhotoIntentService.class);
        List<Destination> destinations = trip.getDestinations();
        if (destinations.size() > 0) {
            placePhotoIntentService.putExtra(Constants.Extras.EXTRA_TRIP, trip);
        }

        startService(placePhotoIntentService);

        finish();
    }

    @Override
    public void deleteTrip(Trip trip) {
        if (trip != null && !TextUtils.isEmpty(trip.getId())) {
            mUserTripReference.child(trip.getId()).removeValue();
            Toast.makeText(this, getString(R.string.trip_deleted_success), Toast
                    .LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.trip_deleted_error), Toast.LENGTH_SHORT).show();
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
