package com.henriquenfaria.wisetrip.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.Trip;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripFactoryFragment extends Fragment implements DatePickerFragment.OnDateSetListener {

    private static final String TAG = TripFactoryFragment.class.getSimpleName();
    private static final String ARG_TRIP = "arg_trip";
    private static final String TAG_DATE_PICKER_FRAGMENT = "tag_date_picker_fragment";
    private static final String SAVE_START_DATE_MILLIS = "save_start_date_millis";
    private static final String SAVE_END_DATE_MILLIS = "save_end_date_millis";

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

    public interface OnTripFactoryListener {
        void changeActionBarTitle(String newTitle);

        void saveTrip(Trip trip, boolean isNewTrip);
    }
}
