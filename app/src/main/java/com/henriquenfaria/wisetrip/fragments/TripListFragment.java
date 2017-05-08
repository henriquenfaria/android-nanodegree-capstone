package com.henriquenfaria.wisetrip.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TripHolder;
import com.henriquenfaria.wisetrip.models.Trip;

public class TripListFragment extends Fragment {

    //TODO: ButterKnife is not working
    //@BindView(R.id.trip_list_recycler_view)
    RecyclerView mTripListRecyclerView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserTripReference;
    private FirebaseUser mCurrentUser;
    private FirebaseRecyclerAdapter<Trip, TripHolder> mAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mUserTripReference = mFirebaseDatabase.getReference()
                .child("user-trips")
                .child(mCurrentUser.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trip_list, container, false);

        //TODO: ButterKnife is not working
        //ButterKnife.bind(this, rootView);

        mTripListRecyclerView = (RecyclerView) rootView.findViewById(R.id.trip_list_recycler_view);

        mTripListRecyclerView.setHasFixedSize(false);
        mTripListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Query query = mUserTripReference.orderByChild("title");
        mAdapter = new FirebaseRecyclerAdapter<Trip, TripHolder>(Trip.class, R.layout
                .trip_item, TripHolder.class, mUserTripReference) {
            @Override
            public void populateViewHolder(TripHolder chatMessageViewHolder, Trip trip, int
                    position) {
                chatMessageViewHolder.setTripTitle(trip.getTitle());
            }
        };

        // TODO: If date is properly indexed, use:
        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
       /* new FirebaseIndexRecyclerAdapter<Trip, TripHolder>(Trip.class,
                R.layout.trip_item,
                TripHolder.class,
                keyRef, // The Firebase location containing the list of keys to be found in dataRef.
                dataRef) //The Firebase location to watch for data changes. Each key key found at
                 keyRef's location represents a list item in the RecyclerView.
         */


        mTripListRecyclerView.setAdapter(mAdapter);


        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }

    }
}
