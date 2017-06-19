package com.henriquenfaria.wisetrip.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TripListSection;
import com.henriquenfaria.wisetrip.models.Destination;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.service.PlacePhotoIntentService;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import timber.log.Timber;

public class TripListFragment extends BaseFragment {


    @BindView(R.id.trip_list_recycler_view)
    protected RecyclerView mTripListRecyclerView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private Query mUserTripQuery;
    private FirebaseUser mCurrentUser;
    private SectionedRecyclerViewAdapter mTripAdapter;
    private ChildEventListener mTripsEventListener;
    private ValueEventListener mTripsValueListener;
    private PlacePhotoReceiver mPlacePhotoReceiver;

    private ArrayList<Trip> mUpcomingTrips;
    private ArrayList<Trip> mCurrentTrips;
    private ArrayList<Trip> mPastTrips;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUpcomingTrips = new ArrayList<>();
        mCurrentTrips = new ArrayList<>();
        mPastTrips = new ArrayList<>();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mUserTripQuery = mFirebaseDatabase.getReference()
                .child("user-trips")
                .child(mCurrentUser.getUid())
                //TODO: Need to order by child?
                .orderByChild("startDate");

        // attachDatabaseReadListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_trip_list, container, false);

        ButterKnife.bind(this, rootView);

        // TODO: Another possible solution for descending ordering is to save a inverted timestamp
        // -1 * new Date().getTime();
        /*LinearLayoutManager reversedLayoutManager = new LinearLayoutManager(mFragmentActivity);
        reversedLayoutManager.setReverseLayout(true);
        reversedLayoutManager.setStackFromEnd(true);*/

        mTripListRecyclerView.setLayoutManager(/*reversedLayoutManager*/new LinearLayoutManager
                (mFragmentActivity));
        mTripListRecyclerView.setHasFixedSize(false);
        mTripAdapter = new SectionedRecyclerViewAdapter();

        // TODO: If date is properly indexed, use:
        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
       /* new FirebaseIndexRecyclerAdapter<mTripAdapter, TripFirebaseHolder>(mTripAdapter.class,
                R.layout.trip_item,
                TripFirebaseHolder.class,
                keyRef, // The Firebase location containing the list of keys to be found in dataRef.
                dataRef) //The Firebase location to watch for data changes. Each key key found at
                 keyRef's location represents a list item in the RecyclerView.
         */

        mTripListRecyclerView.setAdapter(mTripAdapter);

        attachDatabaseReadListener();

        mPlacePhotoReceiver = new PlacePhotoReceiver();

        return rootView;
    }

    private void attachDatabaseReadListener() {
        if (mTripsEventListener == null) {
            mTripsEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildAdded");

                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        Trip.State state = trip.getState(System.currentTimeMillis());
                        int index = addOrderedTripToList(trip, getTripList(state));
                        if (index >= 0) {
                            if (mTripAdapter.getSection(state.name()) instanceof TripListSection) {
                                mTripAdapter.notifyItemInsertedInSection(state.name(), index);
                            } else {
                                recreateSections();
                            }
                        }

                        Intent placePhotoIntentService = new Intent(mFragmentActivity,
                                PlacePhotoIntentService.class);
                        placePhotoIntentService.setAction(Constants.Action.ACTION_ADD_PHOTO);
                        List<Destination> destinations = trip.getDestinations();
                        if (destinations.size() > 0) {
                            placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP, trip);
                            placePhotoIntentService.putExtra(Constants.Extra.EXTRA_UPDATE_TRIP_LIST,
                                    Utils.getBooleanFromSharedPrefs(mFragmentActivity,
                                            Constants.Preferences.SIGN_IN_UPDATE_TRIP_LIST, true));
                            mFragmentActivity.startService(placePhotoIntentService);
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");

                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        if (removeTripFromAllLists(trip)) {
                            Trip.State state = trip.getState(System.currentTimeMillis());
                            int index = addOrderedTripToList(trip, getTripList(state));
                            if (index >= 0) {
                                if (mTripAdapter.getSection(state.name())
                                        instanceof TripListSection) {
                                    mTripAdapter.notifyItemInsertedInSection(state.name(), index);
                                } else {
                                    recreateSections();
                                }
                            }
                        }

                        Intent placePhotoIntentService = new Intent(mFragmentActivity,
                                PlacePhotoIntentService.class);
                        placePhotoIntentService.setAction(Constants.Action.ACTION_CHANGE_PHOTO);
                        placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP, trip);
                        mFragmentActivity.startService(placePhotoIntentService);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");

                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        Trip.State state = trip.getState(System.currentTimeMillis());
                        int index = removeTripFromList(trip, getTripList(state));
                        if (index >= 0) {
                            if (mTripAdapter.getSection(state.name()) instanceof TripListSection) {
                                if (getTripList(state).isEmpty()) {
                                    mTripAdapter.removeSection(state.name());
                                    mTripAdapter.notifyDataSetChanged();
                                } else {
                                    mTripAdapter.notifyItemRemovedFromSection(state.name(), index);
                                    mTripAdapter.notifyItemRangeChangedInSection
                                            (state.name(), index, getTripList(state).size());
                                }
                            }
                        }

                        Intent placePhotoIntentService = new Intent(mFragmentActivity,
                                PlacePhotoIntentService.class);
                        placePhotoIntentService.setAction(Constants.Action.ACTION_REMOVE_PHOTO);
                        placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP, trip);
                        mFragmentActivity.startService(placePhotoIntentService);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildMoved");
                    //TODO: Implementation needed?
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled");
                }

            };
            if (mTripsValueListener == null) {
                mTripsValueListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Force update list after sign in, after initial load, remove force update
                        if (Utils.getBooleanFromSharedPrefs(mFragmentActivity,
                                Constants.Preferences.SIGN_IN_UPDATE_TRIP_LIST, true)) {
                            Utils.saveBooleanToSharedPrefs(mFragmentActivity,
                                    Constants.Preferences.SIGN_IN_UPDATE_TRIP_LIST, false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
            }
            mUserTripQuery.addChildEventListener(mTripsEventListener);
            mUserTripQuery.addValueEventListener(mTripsValueListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mTripsEventListener != null) {
            mUserTripQuery.removeEventListener(mTripsEventListener);
            mTripsEventListener = null;
        }

        if (mTripsValueListener != null) {
            mUserTripQuery.removeEventListener(mTripsValueListener);
            mTripsValueListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachDatabaseReadListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlacePhotoReceiver != null) {
            LocalBroadcastManager.getInstance(mFragmentActivity)
                    .registerReceiver(mPlacePhotoReceiver,
                            new IntentFilter(Constants.Action.ACTION_UPDATE_TRIP_LIST));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlacePhotoReceiver != null) {
            LocalBroadcastManager.getInstance(mFragmentActivity)
                    .unregisterReceiver(mPlacePhotoReceiver);
        }
    }

    private class PlacePhotoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTripAdapter != null) {
                //TODO: Find a way to update just the modified items, not the whole list
                // mTripAdapter.notifyItemChanged(itemPosition));
                mTripAdapter.notifyDataSetChanged();
            }
        }
    }

    // Insert a new trip to the list, sort the list and return the final item position in the list
    private int addOrderedTripToList(Trip trip, ArrayList<Trip> sectionTripList) {
        if (TextUtils.isEmpty(trip.getId())) {
            return -1;
        } else {
            sectionTripList.add(trip);
            Collections.sort(sectionTripList);
            for (int i = 0; i < sectionTripList.size(); i++) {
                if (TextUtils.equals(trip.getId(), sectionTripList.get(i).getId())) {
                    return i;
                }
            }
        }
        return -1;
    }

    // Remove a Trip and return the index position of the trip in the array
    private int removeTripFromList(Trip trip, ArrayList<Trip> sectionTripList) {
        if (TextUtils.isEmpty(trip.getId()) || sectionTripList.size() <= 0) {
            return -1;
        } else {
            for (int i = 0; i < sectionTripList.size(); i++) {
                if (TextUtils.equals(trip.getId(), sectionTripList.get(i).getId())) {
                    sectionTripList.remove(i);
                    return i;
                }
            }
        }
        return -1;
    }

    // Search inside all lists for a Trip and remove it, returns true on a successful removal
    private boolean removeTripFromAllLists(Trip trip) {
        if (TextUtils.isEmpty(trip.getId())) {
            return false;
        } else {
            for (Trip.State state : Trip.State.values()) {
                int index = removeTripFromList(trip, getTripList(state));
                if (index >= 0) {
                    if (mTripAdapter.getSection(state.name()) instanceof TripListSection) {
                        if (getTripList(state).isEmpty()) {
                            mTripAdapter.removeSection(state.name());
                            mTripAdapter.notifyDataSetChanged();
                        } else {
                            mTripAdapter.notifyItemRemovedFromSection(state.name(), index);
                            mTripAdapter.notifyItemRangeChangedInSection
                                    (state.name(), index, getTripList(state).size());
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // We must always keep sections order (Current - Upcoming - Past).
    // To keep the correct order, we must first remove all sections and then add them back in order.
    private void recreateSections() {
        if (mTripAdapter != null) {
            mTripAdapter.removeAllSections();
            for (Trip.State state : Trip.State.values()) {
                // ArrayList<Trip> list = getTripList(state);
                if (!getTripList(state).isEmpty()) {
                    mTripAdapter.addSection(state.name(), new TripListSection
                            (mFragmentActivity, getSectionTitle(mFragmentActivity, state),
                                    getTripList(state)));
                }
            }

            mTripAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<Trip> getTripList(Trip.State state) {
        switch (state) {
            case CURRENT:
                return mCurrentTrips;

            case UPCOMING:
                return mUpcomingTrips;

            case PAST:
                return mPastTrips;

            default:
                Timber.e("Can't find correct Trip list");
                throw new IllegalArgumentException();
        }
    }

    private String getSectionTitle(Context context, Trip.State state) {
        switch (state) {
            case CURRENT:
                return context.getString(R.string.current);

            case UPCOMING:
                return context.getString(R.string.upcoming);

            case PAST:
                return context.getString(R.string.past);

            default:
                Timber.e("Can't find correct Trip title");
                throw new IllegalArgumentException();
        }
    }
}
