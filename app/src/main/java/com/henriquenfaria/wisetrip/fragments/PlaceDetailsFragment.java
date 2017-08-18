package com.henriquenfaria.wisetrip.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.PlaceModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Utils;

import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaceDetailsFragment extends BaseFragment {

    private static final String ARG_PLACE = "arg_place";
    private static final String ARG_TRIP = "arg_trip";

    private static final String SAVE_TRIP = "save_trip";
    private static final String SAVE_PLACE = "save_place";
    private static final String SAVE_IS_EDIT_MODE = "save_is_edit_mode";

    @BindView(R.id.destination_text)
    protected TextView mDestinationTextView;

    @BindView(R.id.date_text)
    protected TextView mDateTextView;

    private TripModel mTrip;
    private PlaceModel mPlace;
    private boolean mIsEditMode;
    private DatePickerDialogFragment mDatePickerFragment;
    private AlertDialogFragment mAlertDialogFragment;

    private OnPlaceDetailsListener mOnPlaceDetailsListener;

    public PlaceDetailsFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance with PlaceModel info
    public static PlaceDetailsFragment newInstance(TripModel trip, PlaceModel place) {
        PlaceDetailsFragment fragment = new PlaceDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        args.putParcelable(ARG_PLACE, place);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_TRIP, mTrip);
        outState.putParcelable(SAVE_PLACE, mPlace);
        outState.putBoolean(SAVE_IS_EDIT_MODE, mIsEditMode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrip = getArguments().getParcelable(ARG_TRIP);
            mPlace = getArguments().getParcelable(ARG_PLACE);

            if (mPlace == null) {
                mPlace = new PlaceModel();
                mPlace.setDate(DateTime.now().withTimeAtStartOfDay().getMillis());
            }

            if (!TextUtils.isEmpty(mPlace.getId())) {
                mIsEditMode = true;
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_place_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            if (mOnPlaceDetailsListener != null) {
                mOnPlaceDetailsListener.onPlaceEditMenu(mTrip, mPlace);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnPlaceDetailsListener = (OnPlaceDetailsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnPlaceDetailsListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {

        // Restore instances
        if (savedInstanceState != null) {
            mTrip = savedInstanceState.getParcelable(SAVE_TRIP);
            mPlace = savedInstanceState.getParcelable(SAVE_PLACE);
            mIsEditMode = savedInstanceState.getBoolean(SAVE_IS_EDIT_MODE);
        }

        View rootView = inflater.inflate(R.layout.fragment_place_details, container, false);
        ButterKnife.bind(this, rootView);

        if (mPlace.getDestination() != null) {
            mOnPlaceDetailsListener.changeActionBarTitle(mPlace.getDestination().getName());
        } else {
            mOnPlaceDetailsListener.changeActionBarTitle(getString(R.string.place_details));
        }

        populateFormFields();

        return rootView;
    }

    private void populateFormFields() {
        mDateTextView.setText(Utils.getFormattedDateText(mPlace.getDate()));

        if (mPlace.getDestination() != null) {
            mDestinationTextView.setText(mPlace.getDestination().getName());
        }
    }

    private boolean isValidFormFields() {
        boolean isValid = true;

        if (mPlace.getDate() <= 0) {
            mDateTextView.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        if (mPlace.getDestination() == null) {
            mDestinationTextView.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        return isValid;
    }


    public interface OnPlaceDetailsListener {
        void changeActionBarTitle(String newTitle);

        void onPlaceEditMenu(TripModel trip, PlaceModel place);
    }
}
