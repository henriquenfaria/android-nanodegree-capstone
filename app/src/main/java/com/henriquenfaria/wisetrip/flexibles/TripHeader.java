package com.henriquenfaria.wisetrip.flexibles;

import android.view.View;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.HeaderModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Header for Trip items
 */
public class TripHeader extends AbstractHeaderItem<TripHeader.HeaderViewHolder>
        implements IFilterable, IHolder<HeaderModel> {

    private HeaderModel mTripHeader;

    public TripHeader(HeaderModel tripHeader) {
        mTripHeader = tripHeader;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TripHeader) {
            TripHeader inItem = (TripHeader) o;
            return mTripHeader.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mTripHeader.hashCode();
    }

    @Override
    public HeaderModel getModel() {
        return mTripHeader;
    }

    @Override
    public boolean filter(String constraint) {
        return mTripHeader.getTitle() != null && mTripHeader.getTitle().equals(constraint);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.trip_header_item;
    }

    @Override
    public HeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new HeaderViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, HeaderViewHolder holder,
                               int position, List payloads) {
        holder.mTitle.setText(mTripHeader.getTitle());
    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        @BindView(R.id.header_title)
        public TextView mTitle;

        public HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, false);//true only for header items when will be sticky
            ButterKnife.bind(this, view);
        }
    }
}