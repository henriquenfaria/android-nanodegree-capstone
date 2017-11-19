package com.henriquenfaria.wisetrip.flexibles;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.PlaceModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Flexible for Place item
 */
public class PlaceItem extends AbstractSectionableItem<PlaceItem.ItemViewHolder, PlaceHeader>
        implements IFilterable, IHolder<PlaceModel> {

    private PlaceModel mPlace;

    public PlaceItem(PlaceModel place, PlaceHeader header) {
        super(header);
        this.mPlace = place;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PlaceItem) {
            PlaceItem inItem = (PlaceItem) o;
            return mPlace.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mPlace.hashCode();
    }


    public PlaceModel getModel() {
        return mPlace;
    }

    @Override
    public boolean filter(String constraint) {
        return mPlace.getDestination().getName() != null
                && mPlace.getDestination().getName().toLowerCase().trim().contains(constraint);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.place_item;
    }

    @Override
    public ItemViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ItemViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, ItemViewHolder holder,
                               int position, List payloads) {
        holder.mTitle.setText(mPlace.getDestination().getName());
    }

    static class ItemViewHolder extends FlexibleViewHolder {

        @BindView(R.id.place_item_layout)
        public RelativeLayout mLayout;

        @BindView(R.id.place_title)
        public TextView mTitle;

        public ItemViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            ButterKnife.bind(this, view);
            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAdapter.mItemClickListener != null) {
                        mAdapter.mItemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }
    }
}