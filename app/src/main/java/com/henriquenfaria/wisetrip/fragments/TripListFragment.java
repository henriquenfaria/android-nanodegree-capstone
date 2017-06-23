package com.henriquenfaria.wisetrip.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.CustomRecyclerView;
import com.henriquenfaria.wisetrip.data.TripListSection;
import com.henriquenfaria.wisetrip.models.Destination;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.service.PlacePhotoIntentService;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import timber.log.Timber;

public class TripListFragment extends BaseFragment {


    @BindView(R.id.trip_list_recycler_view)
    protected CustomRecyclerView mTripListRecyclerView;

    @BindView(R.id.empty_trip_list_text)
    protected TextView mEmptyTripListText;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserTripReference;
    private FirebaseUser mCurrentUser;
    private SectionedRecyclerViewAdapter mTripAdapter;
    private ChildEventListener mTripsEventListener;
    private PlacePhotoReceiver mPlacePhotoReceiver;

    private SortedList<Trip> mUpcomingTrips;
    private SortedList<Trip> mCurrentTrips;
    private SortedList<Trip> mPastTrips;

    private SortedList.Callback<Trip> mAscSortedListCallback = new SortedList.Callback<Trip>() {

        @Override
        public void onInserted(int position, int count) {
            //Do nothing, adapter notify is controlled inside Firebase callbacks
        }

        @Override
        public void onRemoved(int position, int count) {
            //Do nothing, adapter notify is controlled inside Firebase callbacks
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            //Do nothing, adapter notify is controlled inside Firebase callbacks
        }

        @Override
        public void onChanged(int position, int count) {
            //Do nothing, adapter notify is controlled inside Firebase callbacks
        }

        @Override
        public int compare(Trip trip1, Trip trip2) {
            return trip1.compareTo(trip2);
        }

        @Override
        public boolean areContentsTheSame(Trip oldTrip, Trip newTrip) {
            //Asc order
            return oldTrip.equals(newTrip);
        }

        @Override
        public boolean areItemsTheSame(Trip trip1, Trip trip2) {
            return trip1.hashCode() == trip2.hashCode();
        }
    };


    private SortedList.Callback<Trip> mDescSortedListCallback = new SortedList.Callback<Trip>() {

        @Override
        public void onInserted(int position, int count) {
            //Do nothing, adapter notify is controlled inside Firebase callbacks
        }

        @Override
        public void onRemoved(int position, int count) {
            //Do nothing, adapter notify is controlled inside Firebase callbacks
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            //Do nothing, adapter notify is controlled inside Firebase callbacks
        }

        @Override
        public void onChanged(int position, int count) {
            //Do nothing, adapter notify is controlled inside Firebase callbacks
        }

        @Override
        public int compare(Trip trip1, Trip trip2) {
            // Desc order
            return trip2.compareTo(trip1);
        }

        @Override
        public boolean areContentsTheSame(Trip oldTrip, Trip newTrip) {
            return oldTrip.equals(newTrip);
        }

        @Override
        public boolean areItemsTheSame(Trip trip1, Trip trip2) {
            return trip1.hashCode() == trip2.hashCode();
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUpcomingTrips = new SortedList<>(Trip.class, mAscSortedListCallback);
        mCurrentTrips = new SortedList<>(Trip.class, mAscSortedListCallback);
        mPastTrips = new SortedList<>(Trip.class, mDescSortedListCallback);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        //TODO: Need to order by child?
        mUserTripReference = mFirebaseDatabase.getReference()
                .child("user-trips")
                .child(mCurrentUser.getUid());

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
        mTripListRecyclerView.setEmptyView(mEmptyTripListText);

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
                    if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                        Trip.State state = trip.getState(System.currentTimeMillis());
                        SortedList<Trip> currentList = getTripList(state);
                        currentList.add(trip);
                        int index = currentList.add(trip);
                        if (mTripAdapter.getSection(state.name()) instanceof TripListSection) {
                            mTripAdapter.notifyItemInsertedInSection(state.name(), index);
                        } else {
                            recreateSections();
                        }

                        Intent placePhotoIntentService = new Intent(mFragmentActivity,
                                PlacePhotoIntentService.class);
                        placePhotoIntentService.setAction(Constants.Action.ACTION_ADD_PHOTO);
                        List<Destination> destinations = trip.getDestinations();
                        if (destinations.size() > 0) {
                            placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP, trip);
                            mFragmentActivity.startService(placePhotoIntentService);
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");

                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                        if (removeTripFromAllLists(trip)) {
                            Trip.State state = trip.getState(System.currentTimeMillis());
                            SortedList<Trip> currentList = getTripList(state);
                            int index = currentList.add(trip);
                            if (mTripAdapter.getSection(state.name()) instanceof TripListSection) {
                                mTripAdapter.notifyItemInsertedInSection(state.name(), index);
                            } else {
                                recreateSections();
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
                    if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                        Trip.State state = trip.getState(System.currentTimeMillis());
                        SortedList<Trip> currentList = getTripList(state);
                        int index = getIndexOnSortedList(trip, currentList);
                        if (index != SortedList.INVALID_POSITION) {
                            if (mTripAdapter.getSection(state.name()) instanceof TripListSection) {
                                currentList.removeItemAt(index);
                                if (currentList.size() <= 0) {
                                    mTripAdapter.removeSection(state.name());
                                    mTripAdapter.notifyDataSetChanged();
                                } else {
                                    mTripAdapter.notifyItemRemovedFromSection(state.name(), index);
                                    mTripAdapter.notifyItemRangeChangedInSection
                                            (state.name(), index, currentList.size());

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


            mUserTripReference.addChildEventListener(mTripsEventListener);

        }
    }

    private void detachDatabaseReadListener() {
        if (mTripsEventListener != null) {
            mUserTripReference.removeEventListener(mTripsEventListener);
            mTripsEventListener = null;
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

    // Search inside all lists for a Trip and remove it, returns true on a successful removal
    private boolean removeTripFromAllLists(Trip trip) {
        for (Trip.State state : Trip.State.values()) {
            SortedList<Trip> currentList = getTripList(state);
            int index = getIndexOnSortedList(trip, currentList);
            if (index != SortedList.INVALID_POSITION) {
                if (mTripAdapter.getSection(state.name()) instanceof TripListSection) {
                    currentList.removeItemAt(index);
                    if (currentList.size() <= 0) {
                        mTripAdapter.removeSection(state.name());
                        mTripAdapter.notifyDataSetChanged();
                    } else {
                        mTripAdapter.notifyItemRemovedFromSection(state.name(), index);
                        mTripAdapter.notifyItemRangeChangedInSection
                                (state.name(), index, currentList.size());
                    }
                    return true;
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
                SortedList<Trip> currentList = getTripList(state);
                if (currentList.size() > 0) {
                    mTripAdapter.addSection(state.name(), new TripListSection
                            (getSectionTitle(mFragmentActivity, state), currentList));
                }
            }

            mTripAdapter.notifyDataSetChanged();
        }
    }

    // Can't use SortedList.indexOf method. Because we're only checking Trip's id
    private int getIndexOnSortedList(Trip searchTrip, SortedList<Trip> sortedList) {
        for (int i = 0; i < sortedList.size(); i++) {
            if (sortedList.get(i).equals(searchTrip)) {
                return i;
            }
        }
        return SortedList.INVALID_POSITION;
    }

    private SortedList<Trip> getTripList(Trip.State state) {
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

    // Search for a Trip inside all sections lists and update it to make it display latest info
    private void updateTripListItem(Trip trip) {
        if (mTripAdapter != null) {
            for (Trip.State state : Trip.State.values()) {
                SortedList<Trip> currentList = getTripList(state);
                if (currentList.size() > 0) {
                    int index = getIndexOnSortedList(trip, currentList);
                    if (index != SortedList.INVALID_POSITION) {
                        mTripAdapter.notifyItemChangedInSection(state.name(), index);
                        return;
                    }
                }
            }
            mTripAdapter.notifyDataSetChanged();
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

    private class PlacePhotoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Constants.Action.ACTION_UPDATE_TRIP_LIST)) {
                if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                    Trip trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                    updateTripListItem(trip);
                }
            }
        }
    }
}
