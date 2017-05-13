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

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.Trip;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripFactoryFragment extends Fragment {

    private static final String ARG_TRIP = "arg_trip";
    private OnTripFactoryListener mListener;
    private Trip mTrip;



    @BindView(R.id.title_edit_text)
    EditText mTripTitleEditText;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTrip = getArguments().getParcelable(ARG_TRIP);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trip_factory_menu, menu);  // Use filter.xml from step 1
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

        View rootView = inflater.inflate(R.layout.fragment_trip_factory, container, false);
        ButterKnife.bind(this, rootView);

        mListener.changeActionBarTitle(getString(R.string.create_new_trip));

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
            Trip newTrip = new Trip(mTripTitleEditText.getText().toString(), 1494100000000L, 1494192097015L, null);
            mListener.saveTrip(newTrip, true);

        }
    }

    public interface OnTripFactoryListener {
        void changeActionBarTitle(String newTitle);
        void saveTrip(Trip trip, boolean isNewTrip);
    }
}
