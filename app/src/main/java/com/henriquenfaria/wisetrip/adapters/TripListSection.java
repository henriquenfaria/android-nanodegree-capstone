package com.henriquenfaria.wisetrip.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.activities.MainActivity;
import com.henriquenfaria.wisetrip.activities.TripDetailsActivity;
import com.henriquenfaria.wisetrip.activities.TripFactoryActivity;
import com.henriquenfaria.wisetrip.holders.HeaderHolder;
import com.henriquenfaria.wisetrip.holders.TripHolder;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class TripListSection extends StatelessSection {

    private String mTitle;
    private SortedList<TripModel> mTripList;

    public TripListSection(String title, SortedList<TripModel> tripList) {
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
        tripHolder.setTransitionNames(mTripList.get(position).getId());
        tripHolder.setOnTripItemClickListener(new TripHolder.OnTripItemClickListener() {
            @Override
            public void onTripItemClick(View view) {
                Context context = view.getContext();
                Intent tripDetails = new Intent(context, TripDetailsActivity.class);
                tripDetails.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTripList.get
                        (position));
                ActivityCompat.startActivity(context, tripDetails, createTransitionOptions(view));
            }
        });

        tripHolder.setOnEditTripClickListener(new TripHolder.OnEditTripClickListener() {
            @Override
            public void onEditTripClick(View view) {
                MainActivity context = (MainActivity) view.getContext();
                Intent intent = new Intent(context, TripFactoryActivity.class);
                intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTripList.get(position));
                context.startActivityForResult(intent, Constants.Request.REQUEST_TRIP_FACTORY);
            }
        });
    }

    private Bundle createTransitionOptions(View view) {
        View tripPhoto = ButterKnife.findById(view, R.id.trip_photo);
        View tripPhotoProtection = ButterKnife.findById(view, R.id.trip_photo_protection);
        View tripTitle = ButterKnife.findById(view, R.id.trip_title);
        View attributionContainer = ButterKnife.findById(view, R.id.attribution_container);

        String tripPhotoTransition = ViewCompat.getTransitionName(tripPhoto);
        String tripPhotoProtectionTransition = ViewCompat.getTransitionName(tripPhotoProtection);
        String tripTitleTransition = ViewCompat.getTransitionName(tripTitle);
        String attributionContainerTransition = ViewCompat.getTransitionName(attributionContainer);

        Pair p1 = Pair.create(tripPhoto,
                TextUtils.isEmpty(tripPhotoTransition)
                        ? "" : tripPhotoTransition);
        Pair p2 = Pair.create(tripPhotoProtection,
                TextUtils.isEmpty(tripPhotoProtectionTransition)
                        ? "" : tripPhotoProtectionTransition);
        Pair p3 = Pair.create(tripTitle,
                TextUtils.isEmpty(tripTitleTransition)
                        ? "" : tripTitleTransition);
        Pair p4 = Pair.create(attributionContainer,
                TextUtils.isEmpty(attributionContainerTransition)
                        ? "" : attributionContainerTransition);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation((Activity) view.getContext(), p1, p2, p3, p4);

        return options.toBundle();
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
