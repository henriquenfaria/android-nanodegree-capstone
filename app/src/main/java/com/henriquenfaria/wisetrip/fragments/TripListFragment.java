package com.henriquenfaria.wisetrip.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.henriquenfaria.wisetrip.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripListFragment extends Fragment {

    @BindView(R.id.trip_list_recycler_view)
    RecyclerView mTripListRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.fragment_trip_list, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }
}
