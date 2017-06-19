package com.henriquenfaria.wisetrip.data;


import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TripHolder extends RecyclerView.ViewHolder {

    private OnTripItemClickListener mOnTripItemClickListener;
    private OnEditTripClickListener mOnEditTripClickListener;

    @BindView(R.id.trip_card)
    protected CardView mTripCard;

    @BindView(R.id.trip_photo)
    protected ImageView mTripPhoto;

    @BindView(R.id.trip_title)
    protected TextView mTripTitle;

    @BindView(R.id.trip_date)
    protected TextView mTripDate;

    @BindView(R.id.edit_button)
    protected ImageView mEditButton;


    public TripHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        mTripCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnTripItemClickListener.onTripItemClick(v);
            }
        });

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnEditTripClickListener.onEditTripClick(v);
            }
        });
    }

    public void setTripTitle(String tripTitle) {
        mTripTitle.setText(tripTitle);
    }

    public void setTripDate(String tripDate) {
        mTripDate.setText(tripDate);
    }

    public void setTripPhoto(Context context, String tripId) {
        if (!TextUtils.isEmpty(tripId)) {
            ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
            File directoryFile = cw.getDir(Constants.Global.DESTINATION_PHOTO_DIR,
                    Context.MODE_PRIVATE);
            File photoFile = new File(directoryFile, tripId);

            RequestOptions requestOptions =
                    new RequestOptions()
                            .error(R.drawable.trip_card_default)
                            .placeholder(R.color.tripCardPlaceholderBackground)
                            .signature(new ObjectKey(String.valueOf(photoFile.lastModified())))
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

            Glide.with(mTripPhoto.getContext())
                    .load(photoFile)
                    .apply(requestOptions)
                    .into(mTripPhoto);
        }
    }

    public interface OnTripItemClickListener {
        void onTripItemClick(View view);
    }

    public interface OnEditTripClickListener {
        void onEditTripClick(View view);
    }

    public void setOnTripItemClickListener(TripHolder.OnTripItemClickListener
                                                   clickListener) {
        mOnTripItemClickListener = clickListener;
    }

    public void setOnEditTripClickListener(TripHolder.OnEditTripClickListener
                                                   clickListener) {
        mOnEditTripClickListener = clickListener;
    }
}