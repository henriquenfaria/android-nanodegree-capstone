package com.henriquenfaria.wisetrip.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.activities.TripDetailsActivity;
import com.henriquenfaria.wisetrip.activities.TripFactoryActivity;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class TripListSection extends StatelessSection {

    String mTitle;
    SortedList<Trip> mTripList;

    public TripListSection(String title, SortedList<Trip> tripList) {
        super(R.layout.trip_header_item, R.layout.trip_item);
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
        tripHolder.setTripDate(Utils.getFormattedFullTripDateText(mTripList.get(position)
                .getStartDate(), mTripList.get(position).getEndDate()));
        tripHolder.setTripPhoto(mTripList.get(position).getId());
        tripHolder.setOnTripItemClickListener(new TripHolder.OnTripItemClickListener() {
            @Override
            public void onTripItemClick(View view) {
                Context context = view.getContext();
                Intent tripDetails = new Intent(context, TripDetailsActivity.class);
                tripDetails.putExtra(Constants.Extra.EXTRA_TRIP, mTripList.get(position));

                //TODO: Make (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) check?
                View tripPhoto = ButterKnife.findById(view, R.id.trip_photo);
                View tripPhotoProtection = ButterKnife.findById(view, R.id.trip_photo_protection);
                View tripTitle = ButterKnife.findById(view, R.id.trip_title);
                View attributionContainer = ButterKnife.findById(view, R.id.attribution_container);

                Pair<View, String> p1 = Pair.create(tripPhoto,
                        ViewCompat.getTransitionName(tripPhoto));
                Pair<View, String> p2 = Pair.create(tripPhotoProtection,
                        ViewCompat.getTransitionName(tripPhotoProtection));
                Pair<View, String> p3 = Pair.create(tripTitle,
                        ViewCompat.getTransitionName(tripTitle));
                Pair<View, String> p4 = Pair.create(attributionContainer,
                        ViewCompat.getTransitionName(attributionContainer));
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation((Activity) context, p1, p2, p3, p4);
                context.startActivity(tripDetails, options.toBundle());
            }
        });

        tripHolder.setOnEditTripClickListener(new TripHolder.OnEditTripClickListener() {
            @Override
            public void onEditTripClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, TripFactoryActivity.class);
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

}
