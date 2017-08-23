package com.henriquenfaria.wisetrip.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.activities.TravelerActivity;
import com.henriquenfaria.wisetrip.adapters.DestinationAdapter;
import com.henriquenfaria.wisetrip.models.DestinationModel;
import com.henriquenfaria.wisetrip.models.TravelerModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Factory fragment that holds the trip creating form
 */
public class TripFactoryFragment extends BaseFragment implements
        DatePickerDialogFragment.OnDateSetListener,
        DestinationAdapter.OnDestinationClickListener,
        EasyPermissions.PermissionCallbacks {

    private static final String ARG_TRIP = "arg_trip";
    private static final String TAG_DATE_PICKER_FRAGMENT = "tag_date_picker_fragment";
    private static final String TAG_DELETE_ALERT_DIALOG = "tag_delete_alert_dialog";
    private static final String SAVE_TRIP = "save_trip";
    private static final String SAVE_IS_EDIT_MODE = "save_is_edit_mode";
    private static final String SAVE_DESTINATION_ADAPTER_CLICKED_POSITION =
            "save_destination_adapter_clicked_position";
    private static final String SAVE_DISPLAY_DESTINATION_FOOTER_ERROR =
            "save_display_destination_footer_error";

    private static final int PERMISSION_REQUEST_READ_CONTACTS = 1;

    private static final int REQUEST_PICK_TRAVELER = 1;
    private static final int REQUEST_PLACE_AUTOCOMPLETE_UPDATE = 2;
    private static final int REQUEST_PLACE_AUTOCOMPLETE_ADD = 3;

    @BindView(R.id.title_edit_text)
    protected EditText mTripTitleEditText;
    @BindView(R.id.start_date_text)
    protected TextView mStartDateTextView;
    @BindView(R.id.end_date_text)
    protected TextView mEndDateTextView;
    @BindView(R.id.traveler_text)
    protected TextView mTravelerText;
    @BindView(R.id.destination_recyclerview)

    protected RecyclerView mDestinationRecyclerView;
    private OnTripFactoryListener mOnTripFactoryListener;
    private TripModel mTrip;
    private boolean mIsEditMode;
    private DestinationAdapter mDestinationAdapter;
    private int mDestinationAdapterClickedPosition;
    private boolean mIsDisplayDestinationFooterError;

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            DatePickerDialogFragment datePickerFragment = new DatePickerDialogFragment();
            datePickerFragment.setOnDateSetListener(TripFactoryFragment.this);
            datePickerFragment.setTargetViewId(v.getId());

            if (mStartDateTextView.getId() == v.getId()) {
                datePickerFragment.setCurrentDate(mTrip.getStartDate());
                if (mTrip.getEndDate() > 0) {
                    datePickerFragment.setMaximumDate(mTrip.getEndDate());
                }

            } else if (mEndDateTextView.getId() == v.getId()) {
                datePickerFragment.setCurrentDate(mTrip.getEndDate());
                if (mTrip.getStartDate() > 0) {
                    datePickerFragment.setMinimumDate(mTrip.getStartDate());
                }
            }

            datePickerFragment.show(getFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
        }
    };
    private AlertDialogFragment.OnAlertListener mDeleteAlertListener = new AlertDialogFragment
            .OnAlertListener() {
        @Override
        public void positiveAlertButtonClicked() {
            deleteTrip();
        }
    };

    private View.OnClickListener mOnTravelerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            requestReadContactsPermission();
        }
    };
    private TextWatcher mTripTitleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null) {
                mTrip.setTitle(s.toString());
            }
        }
    };

    public TripFactoryFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance with TripModel info
    public static TripFactoryFragment newInstance(TripModel trip) {
        TripFactoryFragment fragment = new TripFactoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    private void requestReadContactsPermission() {
        EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_message),
                PERMISSION_REQUEST_READ_CONTACTS, Manifest.permission.READ_CONTACTS);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_TRIP, mTrip);
        outState.putBoolean(SAVE_IS_EDIT_MODE, mIsEditMode);
        outState.putInt(SAVE_DESTINATION_ADAPTER_CLICKED_POSITION,
                mDestinationAdapterClickedPosition);
        outState.putBoolean(SAVE_DISPLAY_DESTINATION_FOOTER_ERROR,
                mIsDisplayDestinationFooterError);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrip = getArguments().getParcelable(ARG_TRIP);

            if (mTrip == null) {
                mTrip = new TripModel();
            }

            if (!TextUtils.isEmpty(mTrip.getId())) {
                mIsEditMode = true;
            }
        }

        DatePickerDialogFragment datePickerFragment = (DatePickerDialogFragment)
                getFragmentManager().findFragmentByTag(TAG_DATE_PICKER_FRAGMENT);
        if (datePickerFragment != null) {
            datePickerFragment.setOnDateSetListener(this);
        }

        AlertDialogFragment deleteAlertDialogFragment = (AlertDialogFragment)
                getFragmentManager().findFragmentByTag(TAG_DELETE_ALERT_DIALOG);
        if (deleteAlertDialogFragment != null) {
            deleteAlertDialogFragment.setOnAlertListener(mDeleteAlertListener);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_factory_menu, menu);
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
            saveTrip();
            return true;
        } else if (id == R.id.action_delete) {
            createDeleteTripConfirmationDialog(R.string.title_delete_trip, R.string
                    .message_delete_trip);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnTripFactoryListener = (OnTripFactoryListener) context;
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
            mTrip = savedInstanceState.getParcelable(SAVE_TRIP);
            mIsEditMode = savedInstanceState.getBoolean(SAVE_IS_EDIT_MODE);
            mDestinationAdapterClickedPosition
                    = savedInstanceState.getInt(SAVE_DESTINATION_ADAPTER_CLICKED_POSITION);
            mIsDisplayDestinationFooterError = savedInstanceState.getBoolean
                    (SAVE_DISPLAY_DESTINATION_FOOTER_ERROR);
        }

        View rootView = inflater.inflate(R.layout.fragment_trip_factory, container, false);
        ButterKnife.bind(this, rootView);

        if (mIsEditMode) {
            mOnTripFactoryListener.changeActionBarTitle(getString(R.string.edit_trip));
        } else {
            mOnTripFactoryListener.changeActionBarTitle(getString(R.string.create_new_trip));
        }

        mTripTitleEditText.addTextChangedListener(mTripTitleTextWatcher);
        mStartDateTextView.setOnClickListener(mOnDateClickListener);
        mEndDateTextView.setOnClickListener(mOnDateClickListener);
        mTravelerText.setOnClickListener(mOnTravelerClickListener);

        mDestinationAdapter = new DestinationAdapter(mFragmentActivity, this,
                mTrip.getDestinations());
        mDestinationRecyclerView.setAdapter(mDestinationAdapter);
        mDestinationRecyclerView.setLayoutManager(new LinearLayoutManager(mFragmentActivity));

        mDestinationAdapter.setFooterError(mIsDisplayDestinationFooterError);
        mDestinationAdapter.notifyDataSetChanged();

        //if (mIsEditMode && !mIsPopulated) {
        populateFormFields();
        // mIsPopulated = true;
        //}

        return rootView;
    }

    private void populateFormFields() {
        mTripTitleEditText.setText(mTrip.getTitle());
        if (mTrip.getStartDate() > 0) {
            mStartDateTextView.setText(Utils.getFormattedTripDateText(mTrip.getStartDate()));
        }
        if (mTrip.getEndDate() > 0) {
            mEndDateTextView.setText(Utils.getFormattedTripDateText(mTrip.getEndDate()));
        }
        mTravelerText.setText(Utils.getFormattedTravelersText(mTrip.getTravelers()));
    }

    private boolean isValidFormFields() {
        boolean isValid = true;

        if (TextUtils.isEmpty(mTrip.getTitle())) {
            mTripTitleEditText.setError(getString(R.string.mandatory_field));
            isValid = false;
        }
        if (mTrip.getStartDate() <= 0) {
            mStartDateTextView.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        if (mTrip.getEndDate() <= 0) {
            mEndDateTextView.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        if (mTrip.getDestinations().size() == 0) {
            mIsDisplayDestinationFooterError = true;
            mDestinationAdapter.setFooterError(mIsDisplayDestinationFooterError);
            mDestinationAdapter.notifyDataSetChanged();
            isValid = false;
        }

        return isValid;
    }

    private void saveTrip() {
        if (isValidFormFields()) {
            mOnTripFactoryListener.saveTrip(mTrip, mIsEditMode);
        }
    }

    private void deleteTrip() {
        mOnTripFactoryListener.deleteTrip(mTrip);
    }

    private void createDeleteTripConfirmationDialog(int title, int message) {
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.setTitle(title);
        alertDialogFragment.setMessage(message);
        alertDialogFragment.setOnAlertListener(mDeleteAlertListener);
        alertDialogFragment.show(getFragmentManager(), TAG_DELETE_ALERT_DIALOG);
    }

    @Override
    public void onDateSet(int targetViewId, long dateMillis) {
        if (mStartDateTextView.getId() == targetViewId) {
            mTrip.setStartDate(dateMillis);
            mStartDateTextView.setText(Utils.getFormattedTripDateText(dateMillis));
            mStartDateTextView.setError(null);
        } else if (mEndDateTextView.getId() == targetViewId) {
            mTrip.setEndDate(dateMillis);
            mEndDateTextView.setText(Utils.getFormattedTripDateText(dateMillis));
            mEndDateTextView.setError(null);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Response from TravelerActivity
        if (requestCode == REQUEST_PICK_TRAVELER) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.hasExtra(Constants.Extra.EXTRA_TRAVELER)) {
                    // noinspection unchecked
                    mTrip.setTravelers((HashMap<String, TravelerModel>) data.getSerializableExtra
                            (Constants.Extra.EXTRA_TRAVELER));

                    mTravelerText.setText(Utils.getFormattedTravelersText(mTrip.getTravelers()));
                }
            }
            // Response from Place Autocomplete, we'll update a previously set destination
        } else if (requestCode == REQUEST_PLACE_AUTOCOMPLETE_UPDATE) {
            if (resultCode == RESULT_OK) {
                // TODO: Replace all getActivity instances for mFragmentActivity?
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                if (place != null) {
                    //TODO: Must save and use place.getAttributions()
                    DestinationModel destination = new DestinationModel(place);
                    mTrip.getDestinations().set(mDestinationAdapterClickedPosition, destination);
                    mIsDisplayDestinationFooterError = false;
                    mDestinationAdapter.setFooterError(mIsDisplayDestinationFooterError);
                    mDestinationAdapter.swap(mTrip.getDestinations());
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Toast.makeText(mFragmentActivity, R.string.google_play_services_error,
                        Toast.LENGTH_SHORT).show();
                Timber.i(status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation. Nothing to do here.
            }
            // Response from Place Autocomplete, we'll add a new destination
        } else if (requestCode == REQUEST_PLACE_AUTOCOMPLETE_ADD) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                if (place != null) {
                    DestinationModel destination = new DestinationModel(place);
                    mTrip.getDestinations().add(destination);
                    mIsDisplayDestinationFooterError = false;
                    mDestinationAdapter.setFooterError(mIsDisplayDestinationFooterError);
                    mDestinationAdapter.swap(mTrip.getDestinations());
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Toast.makeText(mFragmentActivity, R.string.google_play_services_error,
                        Toast.LENGTH_SHORT).show();
                Timber.i(status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation. Nothing to do here.
            }
        }
    }

    private void startTravelerActivityForResult() {
        Intent intent = new Intent(getContext(), TravelerActivity.class);
        startActivityForResult(intent, REQUEST_PICK_TRAVELER);
    }

    @Override
    public void onDestinationItemClick(int position) {
        mDestinationAdapterClickedPosition = position;
        startPlaceAutocomplete(REQUEST_PLACE_AUTOCOMPLETE_UPDATE);
    }

    @Override
    public void onDestinationRemoveItemClick(int position) {
        if (mTrip.getDestinations() != null && mTrip.getDestinations().get(position) != null) {
            mTrip.getDestinations().remove(position);
            mDestinationAdapter.swap(mTrip.getDestinations());
        }
    }

    @Override
    public void onDestinationFooterClick(int position) {
        startPlaceAutocomplete(REQUEST_PLACE_AUTOCOMPLETE_ADD);
    }

    // TODO: Move startActivityForResult call to TripFactoryActivity?
    private void startPlaceAutocomplete(int requestId) {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .build();

            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(typeFilter)
                    .build(mFragmentActivity);
            startActivityForResult(intent, requestId);
        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            Toast.makeText(mFragmentActivity, R.string.google_play_services_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        startTravelerActivityForResult();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            Toast.makeText(mFragmentActivity, R.string.contacts_permission_denied,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnTripFactoryListener {
        void changeActionBarTitle(String newTitle);

        void saveTrip(TripModel trip, boolean isEditMode);

        void deleteTrip(TripModel trip);
    }
}
