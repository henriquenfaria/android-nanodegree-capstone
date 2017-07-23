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

public class PlaceFactoryFragment extends BaseFragment implements
        DatePickerDialogFragment.OnDateSetListener,
        AlertDialogFragment.OnAlertListener {

    private static final String ARG_PLACE = "arg_place";
    private static final String ARG_TRIP = "arg_trip";

    private static final String TAG_DATE_PICKER_FRAGMENT = "tag_date_picker_fragment";
    private static final String TAG_ALERT_DIALOG_FRAGMENT = "tag_alert_dialog_fragment";
    private static final String SAVE_TRIP = "save_trip";
    private static final String SAVE_PLACE = "save_place";
    private static final String SAVE_IS_EDIT_MODE = "save_is_edit_mode";

    @BindView(R.id.date_text)
    protected TextView mDateTextView;

    private TripModel mTrip;
    private PlaceModel mPlace;
    private boolean mIsEditMode;
    private DatePickerDialogFragment mDatePickerFragment;
    private AlertDialogFragment mAlertDialogFragment;

    private OnPlaceFactoryListener mOnPlaceFactoryListener;

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            mDatePickerFragment = new DatePickerDialogFragment();
            mDatePickerFragment.setOnDateSetListener(PlaceFactoryFragment.this);
            mDatePickerFragment.setCurrentDate(mPlace.getDate());
            mDatePickerFragment.setTargetViewId(v.getId());
            mDatePickerFragment.show(getFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
        }
    };


    public PlaceFactoryFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance with PlaceModel info
    public static PlaceFactoryFragment newInstance(TripModel trip, PlaceModel place) {
        PlaceFactoryFragment fragment = new PlaceFactoryFragment();
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

        mDatePickerFragment = (DatePickerDialogFragment)
                getFragmentManager().findFragmentByTag(TAG_DATE_PICKER_FRAGMENT);
        if (mDatePickerFragment != null) {
            mDatePickerFragment.setOnDateSetListener(this);
        }

        mAlertDialogFragment = (AlertDialogFragment)
                getFragmentManager().findFragmentByTag(TAG_ALERT_DIALOG_FRAGMENT);
        if (mAlertDialogFragment != null) {
            mAlertDialogFragment.setOnAlertListener(this);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trip_factory_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mIsEditMode) {
            menu.findItem(R.id.action_delete).setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            savePlace();
            return true;
        } else if (id == R.id.action_delete) {
            createDeleteTripConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnPlaceFactoryListener = (OnPlaceFactoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnPlaceFactoryListener");
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

        View rootView = inflater.inflate(R.layout.fragment_place_factory, container, false);
        ButterKnife.bind(this, rootView);
        mOnPlaceFactoryListener.changeActionBarTitle(getString(R.string.create_new_place));

        mDateTextView.setOnClickListener(mOnDateClickListener);

        populateFormFields();

        return rootView;
    }

    private void populateFormFields() {
        mDateTextView.setText(Utils.getFormattedExpenseDateText(mPlace.getDate()));
    }

    private boolean isValidFormFields() {
        boolean isValid = true;

        if (mPlace.getDate() <= 0) {
            mDateTextView.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        return isValid;
    }


    private void savePlace() {
        if (isValidFormFields()) {
            mOnPlaceFactoryListener.savePlace(mTrip, mPlace, mIsEditMode);
        }
    }

    private void deleteExpense() {
        mOnPlaceFactoryListener.deletePlace(mTrip, mPlace);
    }

    private void createDeleteTripConfirmationDialog() {
        mAlertDialogFragment = new AlertDialogFragment();
        mAlertDialogFragment.setTitle(R.string.title_delete_place);
        mAlertDialogFragment.setMessage(R.string.message_delete_place);
        mAlertDialogFragment.setOnAlertListener(this);
        mAlertDialogFragment.show(getFragmentManager(), TAG_ALERT_DIALOG_FRAGMENT);
    }


    @Override
    public void onDateSet(int targetViewId, long dateMillis) {
        if (mDateTextView.getId() == targetViewId) {
            mPlace.setDate(new DateTime(dateMillis).withTimeAtStartOfDay().getMillis());
            mDateTextView.setText(Utils.getFormattedExpenseDateText(dateMillis));
            mDateTextView.setError(null);
        }
    }

    @Override
    public void positiveAlertButtonClicked() {
        deleteExpense();
    }


    public interface OnPlaceFactoryListener {
        void changeActionBarTitle(String newTitle);

        void savePlace(TripModel trip, PlaceModel place, boolean isEditMode);

        void deletePlace(TripModel trip, PlaceModel place);
    }
}
