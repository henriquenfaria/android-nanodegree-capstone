package com.henriquenfaria.wisetrip.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.Traveler;

public class TravelerAdapter extends RecyclerView.Adapter<TravelerAdapter.TravelerHolder> {

    private Cursor mCursor;
    private Context mContext;

    /* ViewHolder for each task item */
    public class TravelerHolder extends RecyclerView.ViewHolder {
        public ImageView travelerPhoto;
        public TextView travelerName;

        public TravelerHolder(View itemView) {
            super(itemView);
            travelerPhoto = (ImageView) itemView.findViewById(R.id.traveler_photo);
            travelerName = (TextView) itemView.findViewById(R.id.traveler_name);
        }
    }

    public TravelerAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public TravelerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.traveler_item, parent, false);

        return new TravelerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TravelerHolder holder, int position) {
        if (mCursor == null) {
            return;
        }

        mCursor.moveToPosition(position);
        Traveler traveler = new Traveler(mCursor);

        holder.travelerName.setText(traveler.getName());
        Uri photoUri = traveler.getPhotoUri();

        if (photoUri != null) {
            Glide.with(mContext)
                    .load(photoUri)
                    .apply(new RequestOptions().dontAnimate().centerCrop())
                    .into(holder.travelerPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    public Traveler getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return new Traveler(mCursor);
    }

    /*@Override
    public long getItemId(int position) {
        return getItem(position).id;
    }*/

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
