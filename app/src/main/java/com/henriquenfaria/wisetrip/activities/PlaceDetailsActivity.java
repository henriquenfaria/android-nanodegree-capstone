package com.henriquenfaria.wisetrip.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.PlaceDetailsFragment;
import com.henriquenfaria.wisetrip.models.PlaceModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;

public class PlaceDetailsActivity extends AppCompatActivity
        implements PlaceDetailsFragment.OnPlaceDetailsListener {

    private static final String TAG_PLACE_DETAILS_FRAGMENT = "tag_place_details_fragment";
    private PlaceDetailsFragment mPlaceDetailsFragment;

    private static final int REQUEST_PLACE_DETAILS = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRootReference;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        // Initialize Firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRootReference = mFirebaseDatabase.getReference();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                Toast.makeText(this, R.string.place_loading_error, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // PlaceModel already exists
                PlaceModel place = intent.getParcelableExtra(Constants.Extra.EXTRA_PLACE);
                mPlaceDetailsFragment = PlaceDetailsFragment.newInstance(trip, place);

            } else {
                // New place
                mPlaceDetailsFragment = PlaceDetailsFragment.newInstance(trip, null);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.place_details_fragment_container, mPlaceDetailsFragment,
                            TAG_PLACE_DETAILS_FRAGMENT).commit();
        } else {
            // Fragment already exists, just get it using its TAG
            mPlaceDetailsFragment = (PlaceDetailsFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_PLACE_DETAILS_FRAGMENT);
        }
    }

    @Override
    public void changeActionBarTitle(String newTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(newTitle);
        }
    }

    @Override
    public void onPlaceEditMenu(TripModel trip, PlaceModel place) {
        Intent intent = new Intent(this, PlaceFactoryActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) trip);
        if (place != null) {
            intent.putExtra(Constants.Extra.EXTRA_PLACE, (Parcelable) place);
        }
        startActivityForResult(intent, REQUEST_PLACE_DETAILS);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Response from Place Factory Activity
        if (requestCode == REQUEST_PLACE_DETAILS) {
            if (resultCode == PlaceFactoryActivity.RESULT_PLACE_UPDATED ||
                    resultCode == PlaceFactoryActivity.RESULT_PLACE_DELETED) {
                finish();
            }
        }
    }
}
