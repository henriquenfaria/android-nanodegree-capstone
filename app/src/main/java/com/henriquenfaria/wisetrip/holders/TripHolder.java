package com.henriquenfaria.wisetrip.holders;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
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

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.GlideApp;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.models.AttributionModel;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Item holder for trip list items
 */
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

    public void hideEdit() {
        mEditButton.setVisibility(View.GONE);
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

            GlideApp
                    .with(mTripPhoto.getContext())
                    .load(photoFile)
                    .placeholder(R.color.tripCardPlaceholderBackground)
                    .signature(new ObjectKey(photoFile.lastModified()))
                    .error(R.drawable.trip_photo_default)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target,
                                                    boolean isFirstResource) {
                            displayPhotoAttribution(tripId, false);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target,
                                                       DataSource dataSource,
                                                       boolean isFirstResource) {
                            displayPhotoAttribution(tripId, true);
                            return false;
                        }
                    })
                    .into(mTripPhoto);
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
                        .child(FirebaseDbContract.Attributions.PATH_ATTRIBUTIONS)
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
                        Timber.d("onCancelled", databaseError.getMessage());
                    }
                });
            } else {
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