package com.henriquenfaria.wisetrip.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.henriquenfaria.wisetrip.activities.TripFactoryActivity;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

public class TripFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<Trip, TripFirebaseHolder> {
    public TripFirebaseRecyclerAdapter(Class<Trip> modelClass, int modelLayout,
                                       Class<TripFirebaseHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(TripFirebaseHolder tripViewHolder, final Trip trip,
                                      final int position) {
        tripViewHolder.setTripTitle(trip.getTitle());
        tripViewHolder.setTripDate(Utils.getFormattedFullTripDateText(trip.getStartDate(),
                trip.getEndDate()));

        tripViewHolder.setTripPhoto(trip.getDestinations().get(0).getPhotoReference());

        tripViewHolder.setOnTripItemClickListener(new TripFirebaseHolder.OnTripItemClickListener() {
            @Override
            public void onTripItemClick(View view, int position) {
                // TODO: Remove Toast and call Trip main screen
                Toast.makeText(view.getContext(), trip.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        tripViewHolder.setOnEditTripClickListener(new TripFirebaseHolder.OnEditTripClickListener() {
            @Override
            public void onEditTripClick(View view, int position) {
                Context context = view.getContext();
                Intent intent = new Intent(context, TripFactoryActivity.class);
                intent.putExtra(Constants.Extras.EXTRA_TRIP, trip);
                context.startActivity(intent);
            }
        });
    }
}
