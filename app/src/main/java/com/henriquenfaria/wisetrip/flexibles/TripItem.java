package com.henriquenfaria.wisetrip.flexibles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.activities.MainActivity;
import com.henriquenfaria.wisetrip.activities.TripDetailsActivity;
import com.henriquenfaria.wisetrip.activities.TripFactoryActivity;
import com.henriquenfaria.wisetrip.holders.FlexibleTripHolder;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Features;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.List;

import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHolder;

/**
 * Flexible for Trip item
 */
public class TripItem extends AbstractSectionableItem<FlexibleTripHolder, TripHeader>
        implements IFilterable, IHolder<TripModel> {

    private TripModel mTrip;

    public TripItem(TripModel trip, TripHeader header) {
        super(header);
        this.mTrip = trip;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TripItem) {
            TripItem inItem = (TripItem) o;
            return mTrip.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mTrip.hashCode();
    }


    public TripModel getModel() {
        return mTrip;
    }

    @Override
    public boolean filter(String constraint) {
        return mTrip.getTitle() != null
                && mTrip.getTitle().toLowerCase().trim().contains(constraint);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.trip_item;
    }

    @Override
    public FlexibleTripHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new FlexibleTripHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, FlexibleTripHolder flexibleTripHolder,
                               int position, List payloads) {

        flexibleTripHolder.setTripTitle(mTrip.getTitle());
        flexibleTripHolder.setTripDate(Utils.getFormattedStartEndTripDateText(mTrip
                .getStartDate(), mTrip.getEndDate()));
        flexibleTripHolder.setTripPhoto(mTrip.getId());
        flexibleTripHolder.setTransitionNames(mTrip.getId());
        flexibleTripHolder.setOnTripItemClickListener(new FlexibleTripHolder
                .OnTripItemClickListener() {
            @Override
            public void onTripItemClick(View view) {
                Context context = view.getContext();
                Intent tripDetails = new Intent(context, TripDetailsActivity.class);
                tripDetails.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
                if (Features.TRIP_LIST_SHARED_ELEMENT_TRANSITION_ENABLED) {
                    ActivityCompat.startActivity(context, tripDetails, createTransitionOptions
                            (view));
                } else {
                    context.startActivity(tripDetails);
                }

            }
        });

        flexibleTripHolder.setOnEditTripClickListener(new FlexibleTripHolder
                .OnEditTripClickListener() {
            @Override
            public void onEditTripClick(View view) {
                MainActivity context = (MainActivity) view.getContext();
                Intent intent = new Intent(context, TripFactoryActivity.class);
                intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
                context.startActivity(intent);
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
}