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

public class PlaceHeader extends AbstractHeaderItem<PlaceHeader.HeaderViewHolder>
        implements IFilterable, IHolder<HeaderModel> {

    private HeaderModel mPlaceHeader;

    public PlaceHeader(HeaderModel placeHeader) {
        mPlaceHeader = placeHeader;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PlaceHeader) {
            PlaceHeader inItem = (PlaceHeader) o;
            return mPlaceHeader.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mPlaceHeader.hashCode();
    }

    @Override
    public HeaderModel getModel() {
        return mPlaceHeader;
    }

    @Override
    public boolean filter(String constraint) {
        return mPlaceHeader.getTitle() != null && mPlaceHeader.getTitle().equals(constraint);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.place_header_item;
    }

    @Override
    public HeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new HeaderViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, HeaderViewHolder holder,
                               int position, List payloads) {
        holder.mTitle.setText(mPlaceHeader.getTitle());
    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        @BindView(R.id.place_header_title)
        public TextView mTitle;

        public HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//true only for header items when will be sticky
            ButterKnife.bind(this, view);
        }
    }
}