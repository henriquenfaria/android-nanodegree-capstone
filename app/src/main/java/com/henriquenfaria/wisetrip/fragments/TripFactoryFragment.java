package com.henriquenfaria.wisetrip.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.activities.TravelerActivity;
import com.henriquenfaria.wisetrip.models.Traveler;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class TripFactoryFragment extends Fragment implements DatePickerFragment.OnDateSetListener {

    private static final String TAG = TripFactoryFragment.class.getSimpleName();
    private static final String ARG_TRIP = "arg_trip";
    private static final String TAG_DATE_PICKER_FRAGMENT = "tag_date_picker_fragment";
    private static final String SAVE_START_DATE_MILLIS = "save_start_date_millis";
    private static final String SAVE_END_DATE_MILLIS = "save_end_date_millis";
    private static final int REQUEST_PICK_TRAVELER = 1;
    private static final int REQUEST_READ_CONTACTS = 1;

    private OnTripFactoryListener mListener;
    private Trip mTrip;
    private long mStartDateMillis;
    private long mEndDateMillis;

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.setOnDateSetListener(TripFactoryFragment.this);
            datePickerFragment.setTargetViewId(v.getId());
            if (mStartDateTextView.getId() == v.getId()) {
                datePickerFragment.setCurrentDay(mStartDateMillis);
            } else if (mEndDateTextView.getId() == v.getId()) {
                datePickerFragment.setCurrentDay(mEndDateMillis);
            }

            //TODO: Use getSupportFragmentManager?
            datePickerFragment.show(getFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
        }
    };

    @BindView(R.id.title_edit_text)
    EditText mTripTitleEditText;

    @BindView(R.id.start_date_text)
    TextView mStartDateTextView;

    @BindView(R.id.end_date_text)
    TextView mEndDateTextView;

    @BindView(R.id.traveler_text)
    TextView mTravelerText;

    public TripFactoryFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance with Trip info
    public static TripFactoryFragment newInstance(Trip trip) {
        TripFactoryFragment fragment = new TripFactoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    // Create new Fragment instance
    public static TripFactoryFragment newInstance() {
        TripFactoryFragment fragment = new TripFactoryFragment();
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SAVE_START_DATE_MILLIS, mStartDateMillis);
        outState.putLong(SAVE_END_DATE_MILLIS, mEndDateMillis);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTrip = getArguments().getParcelable(ARG_TRIP);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatePickerFragment datePickerFragment = (DatePickerFragment) getFragmentManager()
                .findFragmentByTag(TAG_DATE_PICKER_FRAGMENT);
        if (datePickerFragment != null) {
            datePickerFragment.setOnDateSetListener(this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trip_factory_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveTrip();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnTripFactoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnTripFactoryListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {

        // Restore instances
        if (savedInstanceState != null) {
            mStartDateMillis = savedInstanceState.getLong(SAVE_START_DATE_MILLIS);
            mEndDateMillis = savedInstanceState.getLong(SAVE_END_DATE_MILLIS);
        }

        View rootView = inflater.inflate(R.layout.fragment_trip_factory, container, false);
        ButterKnife.bind(this, rootView);
        mListener.changeActionBarTitle(getString(R.string.create_new_trip));

        mStartDateTextView.setOnClickListener(mOnDateClickListener);
        mEndDateTextView.setOnClickListener(mOnDateClickListener);

        // TODO: Test code
        mTravelerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(TripFactoryFragment.this.getActivity(),
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(TripFactoryFragment
                                    .this.getActivity(),
                            Manifest.permission.READ_CONTACTS)) {

                        // TODO: Create dialog explaining the permission
                        Toast.makeText(getActivity(), "Permission denied 2", Toast.LENGTH_SHORT)
                                .show();
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(TripFactoryFragment.this.getActivity(),
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_READ_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    startTravelerActivityForResult();

                }
            }
        });

        populateFields();

        return rootView;
    }

    private void populateFields() {
        if (mTrip == null) {
            // New trip, nothing to populate
            return;
        }
    }

    private void saveTrip() {
        // TODO: Must validate fields before calling Activity
        // checkFormFields();

        if (mTrip != null) {
            //TODO: Implement existing trip logic
            //mTrip.setTitle(mTripTitleEditText.getText().toString());
            mListener.saveTrip(mTrip, false);
        } else {
            Trip newTrip = new Trip(mTripTitleEditText.getText().toString(), 1494100000000L,
                    1494192097015L, null);
            mListener.saveTrip(newTrip, true);

        }
    }

    @Override
    public void onDateSet(int targetViewId, long dateMillis, String dateText) {
        // TODO: Store dateMillis which is going to be saved in the db
        if (mStartDateTextView.getId() == targetViewId) {
            mStartDateMillis = dateMillis;
            mStartDateTextView.setText(dateText);
        } else if (mEndDateTextView.getId() == targetViewId) {
            mEndDateMillis = dateMillis;
            mEndDateTextView.setText(dateText);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    startTravelerActivityForResult();


                } else {

                    // TODO: Fix text
                    Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_PICK_TRAVELER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (data != null && data.hasExtra(Constants.Extras.EXTRA_TRAVELER)) {
                    // noinspection unchecked
                    HashMap<Long, Traveler> travelers = (HashMap<Long, Traveler>) data
                            .getSerializableExtra(Constants.Extras.EXTRA_TRAVELER);

                    StringBuffer travelersString = new StringBuffer();
                    int count_for_comma = 0;
                    for (Map.Entry entry : travelers.entrySet()) {
                        Traveler traveler = (Traveler) entry.getValue();
                        travelersString.append(traveler.getName());
                        if (++count_for_comma < travelers.size()) {
                            travelersString.append(", ");
                        }
                    }

                    mTravelerText.setText(travelersString);
                }
            }
        }
    }

    void startTravelerActivityForResult() {
        Intent intent = new Intent(getContext(), TravelerActivity.class);
        startActivityForResult(intent, REQUEST_PICK_TRAVELER);
    }

    public interface OnTripFactoryListener {
        void changeActionBarTitle(String newTitle);

        void saveTrip(Trip trip, boolean isNewTrip);
    }
}
