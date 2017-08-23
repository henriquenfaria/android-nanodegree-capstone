package com.henriquenfaria.wisetrip.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.DestinationModel;
import com.henriquenfaria.wisetrip.models.LatLngModel;
import com.henriquenfaria.wisetrip.models.PlaceModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that displays all the details of a Place
 */
public class PlaceDetailsFragment extends BaseFragment implements OnMapReadyCallback {

    private static final String ARG_PLACE = "arg_place";
    private static final String ARG_TRIP = "arg_trip";

    private static final String SAVE_TRIP = "save_trip";
    private static final String SAVE_PLACE = "save_place";
    private static final String SAVE_IS_EDIT_MODE = "save_is_edit_mode";

    @BindView(R.id.address_container)
    protected LinearLayout mAddressContainer;
    @BindView(R.id.website_container)
    protected LinearLayout mWebsiteContainer;
    @BindView(R.id.phone_container)
    protected LinearLayout mPhoneContainer;

    @BindView(R.id.destination_text)
    protected TextView mDestinationTextView;
    @BindView(R.id.date_text)
    protected TextView mDateTextView;
    @BindView(R.id.address_text)
    protected TextView mAddressTextView;
    @BindView(R.id.website_text)
    protected TextView mWebsiteTextView;
    @BindView(R.id.phone_text)
    protected TextView mPhoneTextView;

    private TripModel mTrip;
    private PlaceModel mPlace;
    private boolean mIsEditMode;

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

        populateDetailsFields();

        initializeMap();

        return rootView;
    }

    private void populateDetailsFields() {
        mDateTextView.setText(Utils.getFormattedDateText(mPlace.getDate()));

        final DestinationModel destination = mPlace.getDestination();
        if (destination != null) {
            if (!TextUtils.isEmpty(destination.getName())) {
                mDestinationTextView.setText(destination.getName());
            }

            if (!TextUtils.isEmpty(destination.getAddress())) {
                // Set hyperlink appearance
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(destination.getAddress());
                ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mAddressTextView.setText(ssb, TextView.BufferType.SPANNABLE);
                
                mAddressContainer.setVisibility(View.VISIBLE);
                mAddressTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Utils.getDestinationMapUri(destination));
                        startActivity(i);
                    }
                });
            }

            if (!TextUtils.isEmpty(destination.getWebsiteUri())) {
                mWebsiteTextView.setText(destination.getWebsiteUri());
                mWebsiteContainer.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(destination.getPhoneNumber())) {
                mPhoneTextView.setText(destination.getPhoneNumber());
                mPhoneContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initializeMap() {
        // Initialize detail map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.place_details_map);
        if (mPlace.getDestination() != null && (mPlace.getDestination().getLatLng() != null)) {
            // Add Maps fragment if place location exists
            mapFragment.getMapAsync(this);
        } else {
            // Hide map fragment if there is no location for this place
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(mapFragment).commit();
        }
    }

    public interface OnPlaceDetailsListener {
        void changeActionBarTitle(String newTitle);

        void onPlaceEditMenu(TripModel trip, PlaceModel place);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mPlace.getDestination() != null && mPlace.getDestination().getLatLng() != null) {
            LatLngModel latLngModel = mPlace.getDestination().getLatLng();
            LatLng location = new LatLng(latLngModel.getLatitude(), latLngModel.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(location)
                    .title(mPlace.getDestination().getName()));
            // .setSnippet(Utils.formatPrice(getContext(), mRealtyItem.getPrice()));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location).zoom(Constants.General.DETAIL_MAP_ZOOM_LEVEL).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.moveCamera(cameraUpdate);

        }
    }
}
