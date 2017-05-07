package com.henriquenfaria.wisetrip.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.henriquenfaria.wisetrip.R;

public class TripListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.fragment_trip_list, container, false);
        //ButterKnife.bind(this, rootView);

        return rootView;
    }
}
