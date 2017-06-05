package com.henriquenfaria.wisetrip.data;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.retrofit.api.PlaceDetailsService;
import com.henriquenfaria.wisetrip.retrofit.models.PlaceDetailsResult;
import com.henriquenfaria.wisetrip.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.henriquenfaria.wisetrip.BuildConfig.GOOGLE_GEO_API_KEY;


public class TripFirebaseHolder extends RecyclerView.ViewHolder {

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

    public void setTripPhoto(String photoReference) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PlaceDetailsService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PlaceDetailsService service = retrofit.create(PlaceDetailsService.class);
        Call<PlaceDetailsResult> call = service.getPhotoResult(photoReference,
                Constants.Global.MAX_PHOTO_HEIGHT, GOOGLE_GEO_API_KEY);

        RequestOptions requestOptions =
                new RequestOptions()
                        .error(R.drawable.trip_card_default)
                        .placeholder(R.color.tripCardPlaceholderBackground)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(mTripPhoto.getContext())
                .load(call.request().url().toString())
                .apply(requestOptions)
                .into(mTripPhoto);
    }

    public interface OnTripItemClickListener {
        void onTripItemClick(View view, int position);
    }

    public interface OnEditTripClickListener {
        void onEditTripClick(View view, int position);
    }

    public void setOnTripItemClickListener(TripFirebaseHolder.OnTripItemClickListener
                                                   clickListener) {
        mOnTripItemClickListener = clickListener;
    }

    public void setOnEditTripClickListener(TripFirebaseHolder.OnEditTripClickListener
                                                   clickListener) {
        mOnEditTripClickListener = clickListener;
    }
}