package com.henriquenfaria.wisetrip.data;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TripFirebaseHolder extends RecyclerView.ViewHolder {

    private OnTripItemClickListener mOnTripItemClickListener;
    private OnEditTripClickListener mOnEditTripClickListener;

    @BindView(R.id.trip_card)
    protected CardView mTripCard;

    @BindView(R.id.trip_title)
    protected TextView mTripTitle;

    @BindView(R.id.trip_date)
    protected TextView mTripDate;

    @BindView(R.id.edit_button)
    protected ImageView mEditButton;


    public TripFirebaseHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        mTripCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTripItemClickListener.onTripItemClick(v, getAdapterPosition());
            }
        });

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnEditTripClickListener.onEditTripClick(v, getAdapterPosition());
            }
        });
    }

    public void setTripTitle(String tripTitle) {
        mTripTitle.setText(tripTitle);
    }

    public void setTripDate(String tripDate) {
        mTripDate.setText(tripDate);
    }


    public interface OnTripItemClickListener {
        void onTripItemClick(View view, int position);
    }

    public interface OnEditTripClickListener {
        void onEditTripClick(View view, int position);
    }

    public void setOnTripItemClickListener(TripFirebaseHolder.OnTripItemClickListener clickListener) {
        mOnTripItemClickListener = clickListener;
    }

    public void setOnEditTripClickListener(TripFirebaseHolder.OnEditTripClickListener clickListener) {
        mOnEditTripClickListener = clickListener;
    }
}