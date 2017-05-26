package com.henriquenfaria.wisetrip.data;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.City;

import java.util.List;


public class DestinationAdapter extends
        RecyclerView.Adapter<DestinationAdapter.DestinationHolder> {

    private static final int FOOTER_VIEW = 1;

    private OnDestinationClickListener mOnDestinationClickListener;
    private List<City> mCities;


    public static class DestinationHolder extends RecyclerView.ViewHolder {
        public TextView destinationText;

        public DestinationHolder(View itemView) {
            super(itemView);
            destinationText = (TextView) itemView.findViewById(R.id.destination_text);
        }
    }


    public static class FooterHolder extends DestinationHolder {


        public FooterHolder(View itemView) {
            super(itemView);

        }
    }

    public static class ItemHolder extends DestinationHolder {

        public ItemHolder(View itemView) {
            super(itemView);

        }
    }

    public DestinationAdapter(OnDestinationClickListener onDestinationClickListener, List<City>
            cities) {
        mOnDestinationClickListener = onDestinationClickListener;
        mCities = cities;
    }

    @Override
    public DestinationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.destination_item, parent, false);

            return new FooterHolder(itemView);

        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.destination_item, parent, false);

            return new ItemHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(final DestinationAdapter.DestinationHolder viewHolder, int
            position) {
        if (viewHolder instanceof FooterHolder) {
            FooterHolder footer = (FooterHolder) viewHolder;
            footer.destinationText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnDestinationClickListener.onDestinationFooterClick(viewHolder
                            .getAdapterPosition());
                }
            });

        } else if (viewHolder instanceof ItemHolder) {
            ItemHolder item = (ItemHolder) viewHolder;
            City city = mCities.get(position);
            item.destinationText.setText(city.getName());
            item.destinationText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnDestinationClickListener.onDestinationItemClick(viewHolder
                            .getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mCities == null) {
            return 0;
        }

        if (mCities.size() == 0) {
            //Return 1 here to show nothing
            return 1;
        }

        // Add extra view to show the footer view
        return mCities.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mCities.size()) {
            // This is where we'll add footer.
            return FOOTER_VIEW;
        }

        return super.getItemViewType(position);
    }

    public void swap(List<City> list) {
        mCities = list;
        notifyDataSetChanged();
    }


    public interface OnDestinationClickListener {
        void onDestinationItemClick(int position);

        void onDestinationFooterClick(int position);
    }
}