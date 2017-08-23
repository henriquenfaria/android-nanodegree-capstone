package com.henriquenfaria.wisetrip.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.henriquenfaria.wisetrip.adapters.TripListSection;
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.models.DestinationModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.services.PlacePhotoIntentService;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;
import com.henriquenfaria.wisetrip.views.SizeAwareRecyclerView;

import org.joda.time.DateTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import timber.log.Timber;

/**
 * Fragment that displays a budget list
 */
public class TripListFragment extends BaseFragment {


    @BindView(R.id.trip_list_recycler_view)
    protected SizeAwareRecyclerView mTripListRecyclerView;

    @BindView(R.id.empty_trip_list_text)
    protected TextView mEmptyTripListText;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTripsReference;
    private FirebaseUser mCurrentUser;
    private SectionedRecyclerViewAdapter mTripAdapter;
    private ChildEventListener mTripsEventListener;
    private PlacePhotoReceiver mPlacePhotoReceiver;

    private SortedList<TripModel> mUpcomingTrips;
    private SortedList<TripModel> mCurrentTrips;
    private SortedList<TripModel> mPastTrips;

    private SortedList.Callback<TripModel> mAscSortedListCallback = new SortedList
            .Callback<TripModel>() {

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
        public int compare(TripModel trip1, TripModel trip2) {
            return trip1.compareTo(trip2);
        }

        @Override
        public boolean areContentsTheSame(TripModel oldTrip, TripModel newTrip) {
            //Asc order
            return oldTrip.equals(newTrip);
        }

        @Override
        public boolean areItemsTheSame(TripModel trip1, TripModel trip2) {
            return trip1.hashCode() == trip2.hashCode();
        }
    };


    private SortedList.Callback<TripModel> mDescSortedListCallback = new SortedList
            .Callback<TripModel>() {

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
        public int compare(TripModel trip1, TripModel trip2) {
            // Desc order
            return trip2.compareTo(trip1);
        }

        @Override
        public boolean areContentsTheSame(TripModel oldTrip, TripModel newTrip) {
            return oldTrip.equals(newTrip);
        }

        @Override
        public boolean areItemsTheSame(TripModel trip1, TripModel trip2) {
            return trip1.hashCode() == trip2.hashCode();
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUpcomingTrips = new SortedList<>(TripModel.class, mAscSortedListCallback);
        mCurrentTrips = new SortedList<>(TripModel.class, mAscSortedListCallback);
        mPastTrips = new SortedList<>(TripModel.class, mDescSortedListCallback);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        //TODO: Need to order by child?
        mTripsReference = mFirebaseDatabase.getReference()
                .child(FirebaseDbContract.Trips.PATH_TRIPS)
                .child(mCurrentUser.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_trip_list, container, false);
        ButterKnife.bind(this, rootView);

        // TODO: If date is properly indexed, use:
        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
       /* new FirebaseIndexRecyclerAdapter<mTripAdapter, TripFirebaseHolder>(mTripAdapter.class,
                R.layout.trip_item,
                TripFirebaseHolder.class,
                keyRef, // The Firebase location containing the list of keys to be found in dataRef.
                dataRef) //The Firebase location to watch for data changes. Each key key found at
                 keyRef's location represents a list item in the RecyclerView.
         */

        mTripListRecyclerView.setLayoutManager(new LinearLayoutManager(mFragmentActivity));
        mTripListRecyclerView.setHasFixedSize(false);
        mTripAdapter = new SectionedRecyclerViewAdapter();
        mTripListRecyclerView.setEmptyView(mEmptyTripListText);
        mTripListRecyclerView.setAdapter(mTripAdapter);
        mPlacePhotoReceiver = new PlacePhotoReceiver();

        // TODO: Move to onCreate()?
        attachDatabaseReadListener();

        return rootView;
    }

    private void attachDatabaseReadListener() {
        if (mTripsEventListener == null) {
            mTripsEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildAdded");
                    TripModel trip = dataSnapshot.getValue(TripModel.class);
                    if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                        TripModel.State state = trip.getState(DateTime.now().getMillis());
                        SortedList<TripModel> currentList = getTripList(state);
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
                        List<DestinationModel> destinations = trip.getDestinations();
                        if (destinations.size() > 0) {
                            placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP,
                                    (Parcelable) trip);
                            mFragmentActivity.startService(placePhotoIntentService);
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");

                    TripModel trip = dataSnapshot.getValue(TripModel.class);
                    if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                        if (removeTripFromAllLists(trip)) {
                            TripModel.State state = trip.getState(DateTime.now().getMillis());
                            SortedList<TripModel> currentList = getTripList(state);
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
                        placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable)
                                trip);
                        mFragmentActivity.startService(placePhotoIntentService);

                        Utils.updateAppWidgets(mFragmentActivity, trip.getId(), false);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");

                    TripModel trip = dataSnapshot.getValue(TripModel.class);
                    if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                        TripModel.State state = trip.getState(DateTime.now().getMillis());
                        SortedList<TripModel> currentList = getTripList(state);
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
                        placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable)
                                trip);
                        mFragmentActivity.startService(placePhotoIntentService);

                        Utils.updateAppWidgets(mFragmentActivity, trip.getId(), true);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildMoved");
                    //TODO: Implementation needed?
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());
                }

            };

            mTripsReference.addChildEventListener(mTripsEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mTripsEventListener != null) {
            mTripsReference.removeEventListener(mTripsEventListener);
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

    // Search inside all lists for a TripModel and remove it, returns true on a successful removal
    private boolean removeTripFromAllLists(TripModel trip) {
        for (TripModel.State state : TripModel.State.values()) {
            SortedList<TripModel> currentList = getTripList(state);
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
            for (TripModel.State state : TripModel.State.values()) {
                SortedList<TripModel> currentList = getTripList(state);
                if (currentList.size() > 0) {
                    mTripAdapter.addSection(state.name(), new TripListSection
                            (getSectionTitle(mFragmentActivity, state), currentList));
                }
            }

            mTripAdapter.notifyDataSetChanged();
        }
    }

    // TODO: Must improve search performance. Use binary search?
    // Can't use SortedList.indexOf method. Because we're only checking TripModel's id
    private int getIndexOnSortedList(TripModel searchTrip, SortedList<TripModel> sortedList) {
        for (int i = 0; i < sortedList.size(); i++) {
            if (sortedList.get(i).equals(searchTrip)) {
                return i;
            }
        }
        return SortedList.INVALID_POSITION;
    }

    private SortedList<TripModel> getTripList(TripModel.State state) {
        switch (state) {
            case CURRENT:
                return mCurrentTrips;

            case UPCOMING:
                return mUpcomingTrips;

            case PAST:
                return mPastTrips;

            default:
                Timber.e("Can't find correct TripModel list");
                throw new IllegalArgumentException();
        }
    }

    // Search for a TripModel inside all sections lists and update it to make it display latest info
    private void updateTripListItem(TripModel trip) {
        if (mTripAdapter != null) {
            for (TripModel.State state : TripModel.State.values()) {
                SortedList<TripModel> currentList = getTripList(state);
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

    private String getSectionTitle(Context context, TripModel.State state) {
        switch (state) {
            case CURRENT:
                return context.getString(R.string.current);
            case UPCOMING:
                return context.getString(R.string.upcoming);
            case PAST:
                return context.getString(R.string.past);
            default:
                Timber.e("Can't find correct TripModel title");
                throw new IllegalArgumentException();
        }
    }

    private class PlacePhotoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Constants.Action.ACTION_UPDATE_TRIP_LIST)) {
                if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                    TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                    updateTripListItem(trip);
                }
            }
        }
    }
}
