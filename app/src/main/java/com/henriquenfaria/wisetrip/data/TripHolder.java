package com.henriquenfaria.wisetrip.data;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;


public class TripHolder extends RecyclerView.ViewHolder {
    private final TextView mTitle;

    public TripHolder(View itemView) {
        super(itemView);
        mTitle = (TextView) itemView.findViewById(R.id.title);
    }

    public void setTripTitle(String title) {
        mTitle.setText(title);
    }
}