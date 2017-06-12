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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TripFirebaseHolder;
import com.henriquenfaria.wisetrip.data.TripFirebaseRecyclerAdapter;
import com.henriquenfaria.wisetrip.models.Destination;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.service.PlacePhotoIntentService;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class TripListFragment extends BaseFragment {

    @BindView(R.id.trip_list_recycler_view)
    protected RecyclerView mTripListRecyclerView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserTripReference;
    private FirebaseUser mCurrentUser;
    private TripFirebaseRecyclerAdapter mTripAdapter;
    private ChildEventListener mTripsEventListener;
    private PlacePhotoReceiver mPlacePhotoReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mUserTripReference = mFirebaseDatabase.getReference()
                .child("user-trips")
                .child(mCurrentUser.getUid());

        attachDatabaseReadListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_trip_list, container, false);

        ButterKnife.bind(this, rootView);

        mTripListRecyclerView.setHasFixedSize(false);
        mTripListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mTripAdapter = new TripFirebaseRecyclerAdapter(Trip.class, R.layout.trip_item,
                TripFirebaseHolder.class, mUserTripReference) {

        };

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
                    if (trip != null) {
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

        if (mTripAdapter != null) {
            mTripAdapter.cleanup();
        }

        detachDatabaseReadListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlacePhotoReceiver != null) {
            LocalBroadcastManager.getInstance(mFragmentActivity)
                    .registerReceiver(mPlacePhotoReceiver, new IntentFilter(Constants.Action.ACTION_PLACE_PHOTO_RESULT));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlacePhotoReceiver != null) {
            LocalBroadcastManager.getInstance(mFragmentActivity).unregisterReceiver(mPlacePhotoReceiver);
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
}
