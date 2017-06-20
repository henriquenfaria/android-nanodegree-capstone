package com.henriquenfaria.wisetrip.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.Traveler;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class TravelerAdapter extends RecyclerView.Adapter<TravelerAdapter.TravelerHolder> {

    private Cursor mCursor;
    private Context mContext;

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
                HashMap<String, Traveler> travelerHashMap = null;
                if (mContext instanceof OnTravelerAdapter) {
                    travelerHashMap = ((OnTravelerAdapter) mContext).getTravelerHashMap();
                }

                if (travelerHashMap != null) {

                    if (travelerHashMap.get(traveler.getContactId()) != null) {
                        //Selected
                        travelerHashMap.remove(traveler.getContactId());
                        ((OnTravelerAdapter) mContext).setTravelerHashMap(travelerHashMap);
                        holder.rootView.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        if (travelerHashMap.size() < Constants.Global.MAX_TRAVELERS) {
                            //Not selected
                            travelerHashMap.put(traveler.getContactId(), traveler);
                            ((OnTravelerAdapter) mContext).setTravelerHashMap(travelerHashMap);
                            holder.rootView.setBackgroundColor(Color.LTGRAY);
                        } else {
                            Toast.makeText(mContext,
                                    R.string.reached_maximum_number_of_travelers,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        HashMap<String, Traveler> travelerHashMap = null;
        if (mContext instanceof OnTravelerAdapter) {
            travelerHashMap = ((OnTravelerAdapter) mContext).getTravelerHashMap();
        }

        if (travelerHashMap != null && travelerHashMap.get(traveler.getContactId()) != null) {
            holder.rootView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.rootView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.travelerName.setText(traveler.getName());

        Picasso.with(mContext)
                .load(traveler.getPhotoUri())
                .placeholder(R.drawable.ic_default_traveler_photo)
                .error(R.drawable.ic_default_traveler_photo)
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
        try {
            return Long.parseLong(getItem(position).getContactId());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public interface OnTravelerAdapter {
        HashMap<String, Traveler> getTravelerHashMap();

        void setTravelerHashMap(HashMap<String, Traveler> travelerHashMap);
    }

    public class TravelerHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.traveler_item)
        protected View rootView;

        @BindView(R.id.traveler_photo)
        protected CircleImageView travelerPhoto;

        @BindView(R.id.traveler_name)
        protected TextView travelerName;

        public TravelerHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
