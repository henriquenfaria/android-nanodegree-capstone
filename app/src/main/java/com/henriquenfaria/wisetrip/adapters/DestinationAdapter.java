package com.henriquenfaria.wisetrip.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.DestinationModel;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.util.List;

import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Adapter for Destination items inside trip factory
 */
public class DestinationAdapter extends
        RecyclerView.Adapter<DestinationAdapter.DestinationHolder> {

    private static final int FOOTER_VIEW_TYPE = 0;
    private static final int ITEM_VIEW_TYPE = 1;

    private Context mContext;
    private OnDestinationClickListener mOnDestinationClickListener;
    private List<DestinationModel> mDestinations;
    private boolean mIsFooterError;


    public DestinationAdapter(Context context, OnDestinationClickListener
            onDestinationClickListener, List<DestinationModel> destinations) {
        mContext = context;
        mOnDestinationClickListener = onDestinationClickListener;
        mDestinations = destinations;
    }

    @Override
    public DestinationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case FOOTER_VIEW_TYPE:
                View footer = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.destination_footer_item, parent, false);
                return new FooterHolder(footer);
            case ITEM_VIEW_TYPE:
                View destination = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.destination_item, parent, false);
                return new ItemHolder(destination);
            default:
                Timber.e("Unknown viewType");
                throw new IllegalStateException();
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

            footer.destinationText.setError(mIsFooterError ?
                    mContext.getString(R.string.mandatory_field) : null);

            if (mDestinations != null && mDestinations.size() > 0) {
                footer.destinationText.setHint(R.string.hint_where_are_you_going);
            } else {
                footer.destinationText.setHint(R.string.hint_where_are_you_going_required);
            }

        } else if (viewHolder instanceof ItemHolder) {
            ItemHolder item = (ItemHolder) viewHolder;
            DestinationModel destination = mDestinations.get(position);
            item.descriptionText.setText(String.format(mContext.getResources()
                    .getString(R.string.destination_item_description), position + 1));

            item.destinationText.setText(destination.getName());
            item.destinationText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnDestinationClickListener.onDestinationItemClick(viewHolder
                            .getAdapterPosition());
                }
            });

            item.removeDestinationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnDestinationClickListener.onDestinationRemoveItemClick(viewHolder
                            .getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mDestinations == null) {
            return 0;
        }

        if (mDestinations.size() == 0) {
            //Return 1 here to show only footer
            return 1;
        }

        if (mDestinations.size() >= Constants.General.MAX_DESTINATIONS) {
            // Remove footer to limit destination number
            return mDestinations.size();
        }

        // Show destination and footer
        return mDestinations.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDestinations.size()) {
            // Footer will be in the last position
            return FOOTER_VIEW_TYPE;
        } else {
            return ITEM_VIEW_TYPE;
        }
    }

    public void swap(List<DestinationModel> list) {
        mDestinations = list;
        notifyDataSetChanged();
    }

    public void setFooterError(boolean footerError) {
        mIsFooterError = footerError;
    }

    public interface OnDestinationClickListener {
        void onDestinationItemClick(int position);

        void onDestinationRemoveItemClick(int position);

        void onDestinationFooterClick(int position);
    }

    public static class DestinationHolder extends RecyclerView.ViewHolder {
        public TextView destinationText;

        public DestinationHolder(View itemView) {
            super(itemView);
            destinationText = ButterKnife.findById(itemView, R.id.destination_text);
        }
    }

    public static class FooterHolder extends DestinationHolder {
        public FooterHolder(View itemView) {
            super(itemView);
        }
    }

    public static class ItemHolder extends DestinationHolder {
        public TextView descriptionText;
        public ImageView removeDestinationButton;

        public ItemHolder(View itemView) {
            super(itemView);
            descriptionText = ButterKnife.findById(itemView, R.id.destination_description_text);
            removeDestinationButton = ButterKnife.findById(itemView,
                    R.id.remove_destination_button);
        }
    }
}