package com.henriquenfaria.wisetrip.data;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.activities.TripFactoryActivity;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class TripListSection extends StatelessSection {

    //TODO: Is it safe? What about memory leak on orientation change?
    Context mContext;
    String mTitle;
    List<Trip> mTripList;


    public TripListSection(Context context, String title, List<Trip> tripList) {
        super(R.layout.trip_header_item, R.layout.trip_item);
        mContext = context;
        mTitle = title;
        mTripList = tripList;
    }

    @Override
    public int getContentItemsTotal() {
        return mTripList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new TripHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final TripHolder tripHolder = (TripHolder) holder;

        tripHolder.setTripTitle(mTripList.get(position).getTitle());
        tripHolder.setTripDate(Utils.getFormattedFullTripDateText(mTripList.get(position).getStartDate(),
                mTripList.get(position).getEndDate()));
        tripHolder.setTripPhoto(mTripList.get(position).getId());
        tripHolder.setOnTripItemClickListener(new TripHolder.OnTripItemClickListener() {
            @Override
            public void onTripItemClick(View view) {
                // TODO: Remove Toast and call Trip main screen
                Toast.makeText(view.getContext(), mTripList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        tripHolder.setOnEditTripClickListener(new TripHolder.OnEditTripClickListener() {
            @Override
            public void onEditTripClick(View view) {
                Context context = view.getContext();

                Intent intent = new Intent(context, TripFactoryActivity.class);
               // sectionAdapter.getPositionInSection(itemHolder.getAdapterPosition())
                intent.putExtra(Constants.Extra.EXTRA_TRIP, mTripList.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderHolder headerHolder = (HeaderHolder) holder;
        headerHolder.setHeaderTitle(mTitle);
    }

    /*public List<Trip> getTripList() {
        return mTripList;
    }

    public void setTripList(List<Trip> tripList) {
        mTripList = tripList;
    }*/

}
