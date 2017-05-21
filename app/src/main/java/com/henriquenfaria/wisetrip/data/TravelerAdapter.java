package com.henriquenfaria.wisetrip.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.Traveler;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class TravelerAdapter extends RecyclerView.Adapter<TravelerAdapter.TravelerHolder> {

    private Cursor mCursor;
    private Context mContext;

    public class TravelerHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.traveler_item)
        View rootView;

        @BindView(R.id.traveler_photo)
        CircleImageView travelerPhoto;

        @BindView(R.id.traveler_name)
        TextView travelerName;

        public TravelerHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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
    public void onBindViewHolder(final TravelerHolder holder, int position) {
        if (mCursor == null) {
            return;
        }

        mCursor.moveToPosition(position);
        final Traveler traveler = new Traveler(mCursor);

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseArray<Traveler> travelerSparseArray = null;
                if (mContext instanceof OnTravelerAdapter) {
                    travelerSparseArray = ((OnTravelerAdapter) mContext).getTravelerSparseArray();
                }

                if (travelerSparseArray != null) {
                    if (travelerSparseArray.get(holder.getAdapterPosition()) != null) {
                        //Selected
                        travelerSparseArray.delete(holder.getAdapterPosition());
                        ((OnTravelerAdapter) mContext).setTravelerSparseArray(travelerSparseArray);
                        holder.rootView.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        //Not selected
                        travelerSparseArray.put(holder.getAdapterPosition(), traveler);
                        ((OnTravelerAdapter) mContext).setTravelerSparseArray(travelerSparseArray);
                        holder.rootView.setBackgroundColor(Color.LTGRAY);
                    }
                }
            }
        });

        SparseArray<Traveler> travelerSparseArray = null;
        if (mContext instanceof OnTravelerAdapter) {
            travelerSparseArray = ((OnTravelerAdapter) mContext).getTravelerSparseArray();
        }

        if (travelerSparseArray != null && travelerSparseArray.get(position) != null) {
            holder.rootView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.rootView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.travelerName.setText(traveler.getName());
        RequestOptions requestOptions =
                new RequestOptions()
                        .dontAnimate()
                        .placeholder(R.drawable.ic_default_traveler)
                        .error(R.drawable.ic_default_traveler);
        Glide.with(mContext)
                .load(traveler.getPhotoUri())
                .apply(requestOptions)
                .into(holder.travelerPhoto);
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

    @Override
    public long getItemId(int position) {
        return getItem(position).getPosition();
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public interface OnTravelerAdapter {
        SparseArray<Traveler> getTravelerSparseArray();

        void setTravelerSparseArray(SparseArray<Traveler> travelerSparseArray);
    }
}
