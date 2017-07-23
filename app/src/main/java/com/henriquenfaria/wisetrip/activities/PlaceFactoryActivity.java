package com.henriquenfaria.wisetrip.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.PlaceFactoryFragment;
import com.henriquenfaria.wisetrip.models.PlaceModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;

public class PlaceFactoryActivity extends AppCompatActivity
        implements PlaceFactoryFragment.OnPlaceFactoryListener {

    private static final String TAG_PLACE_FACTORY_FRAGMENT = "tag_place_factory_fragment";
    private TripModel mTrip;
    private PlaceModel mPlace;
    private PlaceFactoryFragment mPlaceFactoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_factory);

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

            mTrip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);

            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // PlaceModel already exists
                mPlace = intent.getParcelableExtra(Constants.Extra.EXTRA_PLACE);
                mPlaceFactoryFragment = PlaceFactoryFragment.newInstance(mTrip, mPlace);

            } else {
                // New place
                mPlaceFactoryFragment = PlaceFactoryFragment.newInstance(mTrip, null);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.place_factory_fragment_container, mPlaceFactoryFragment,
                            TAG_PLACE_FACTORY_FRAGMENT).commit();
        } else {
            // Fragment already exists, just get it using its TAG
            mPlaceFactoryFragment = (PlaceFactoryFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_PLACE_FACTORY_FRAGMENT);
        }
    }


    @Override
    public void changeActionBarTitle(String newTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(newTitle);
        }
    }

    @Override
    public void savePlace(TripModel trip, PlaceModel place, boolean isEditMode) {
        if (place != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_PLACE, (Parcelable) place);
            setResult(isEditMode ? Constants.Result.RESULT_PLACE_CHANGED
                    : Constants.Result.RESULT_PLACE_ADDED, resultIntent);
        } else {
            setResult(Constants.Result.RESULT_PLACE_ERROR);
        }

        finish();
    }

    @Override
    public void deletePlace(TripModel trip, PlaceModel place) {
        if (place != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_PLACE, (Parcelable) place);
            setResult(Constants.Result.RESULT_PLACE_REMOVED, resultIntent);
        } else {
            setResult(Constants.Result.RESULT_PLACE_ERROR);
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
