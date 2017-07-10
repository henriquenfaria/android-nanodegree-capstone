package com.henriquenfaria.wisetrip.holders;


import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.AttributionModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class TripHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.trip_card)
    protected CardView mTripCard;
    @BindView(R.id.trip_photo)
    protected ImageView mTripPhoto;
    @BindView(R.id.trip_photo_protection)
    protected View mTripPhotoProtection;
    @BindView(R.id.trip_title)
    protected TextView mTripTitle;
    @BindView(R.id.trip_date)
    protected TextView mTripDate;
    @BindView(R.id.attribution_container)
    protected LinearLayout mAttributionContainer;
    @BindView(R.id.attribution_prefix)
    protected TextView mAttributionPrefix;
    @BindView(R.id.attribution_content)
    protected TextView mAttributionContent;
    @BindView(R.id.edit_button)
    protected ImageView mEditButton;


    private OnTripItemClickListener mOnTripItemClickListener;
    private OnEditTripClickListener mOnEditTripClickListener;


    public TripHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        mTripCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnTripItemClickListener != null) {
                    mOnTripItemClickListener.onTripItemClick(v);
                }
            }
        });

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEditTripClickListener != null) {
                    mOnEditTripClickListener.onEditTripClick(v);
                }
            }
        });
    }

    public void setTripTitle(String tripTitle) {
        mTripTitle.setText(tripTitle);
    }

    public void setTripDate(String tripDate) {
        mTripDate.setText(tripDate);
    }

    public void setTransitionNames(String tripId) {
        ViewCompat.setTransitionName(mTripPhoto, Constants.Transition.PREFIX_TRIP_PHOTO + tripId);
        ViewCompat.setTransitionName(mTripPhotoProtection, Constants.Transition
                .PREFIX_TRIP_PHOTO_PROTECTION + tripId);
        ViewCompat.setTransitionName(mAttributionContainer,
                Constants.Transition.PREFIX_TRIP_ATTRIBUTION + tripId);
        ViewCompat.setTransitionName(mTripTitle, Constants.Transition.PREFIX_TRIP_TITLE + tripId);
    }

    public void setTripPhoto(final String tripId) {
        if (!TextUtils.isEmpty(tripId)) {
            ContextWrapper cw = new ContextWrapper(mTripPhoto.getContext().getApplicationContext());
            File directoryFile = cw.getDir(Constants.General.DESTINATION_PHOTO_DIR,
                    Context.MODE_PRIVATE);
            final File photoFile = new File(directoryFile, tripId);

            Picasso.with(mTripPhoto.getContext())
                    .load(photoFile)
                    .networkPolicy(
                            NetworkPolicy.NO_CACHE,
                            NetworkPolicy.NO_STORE,
                            NetworkPolicy.OFFLINE)
                    //.noFade()
                    .placeholder(R.color.tripCardPlaceholderBackground)
                    .error(R.drawable.trip_photo_default)
                    .into(mTripPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            displayPhotoAttribution(tripId, true);
                        }

                        @Override
                        public void onError() {
                            displayPhotoAttribution(tripId, false);
                        }
                    });
        }
    }

    public void setOnTripItemClickListener(TripHolder.OnTripItemClickListener clickListener) {
        mOnTripItemClickListener = clickListener;
    }

    public void setOnEditTripClickListener(TripHolder.OnEditTripClickListener clickListener) {
        mOnEditTripClickListener = clickListener;
    }

    private void displayPhotoAttribution(String tripId, boolean shouldDisplay) {
        if (mAttributionContainer != null) {
            if (shouldDisplay) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference attributionsReference = firebaseDatabase
                        .getReference()
                        .child("attributions")
                        .child(currentUser.getUid())
                        .child(tripId);

                attributionsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.d("onDataChange");
                        if (mAttributionContainer != null) {
                            AttributionModel attribution = dataSnapshot.getValue(AttributionModel
                                    .class);
                            if (attribution != null && !TextUtils.isEmpty(attribution.getText())) {
                                Spanned result;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    result = Html.fromHtml(attribution.getText(),
                                            Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result = Html.fromHtml(attribution.getText());
                                }

                                mAttributionContent.setText(result);
                                mAttributionContent.setMovementMethod(
                                        LinkMovementMethod.getInstance());
                                // mAttributionContainer.setVisibility(View.VISIBLE);

                                mAttributionPrefix.setVisibility(View.VISIBLE);
                                mAttributionContent.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.d("onCancelled");
                        //TODO: Implementation needed?
                    }
                });
            } else {
                //mAttributionContainer.setVisibility(View.GONE);
                mAttributionPrefix.setVisibility(View.GONE);
                mAttributionContent.setVisibility(View.GONE);
            }
        }
    }

    public interface OnTripItemClickListener {
        void onTripItemClick(View view);
    }

    public interface OnEditTripClickListener {
        void onEditTripClick(View view);
    }
}