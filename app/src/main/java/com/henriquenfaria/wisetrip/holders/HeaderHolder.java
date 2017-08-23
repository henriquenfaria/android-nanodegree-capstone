package com.henriquenfaria.wisetrip.holders;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Header holder for trip list sections
 */
public class HeaderHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.header_title)
    protected TextView mHeaderTitle;

    public HeaderHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setHeaderTitle(String headerTitle) {
        mHeaderTitle.setText(headerTitle);
    }
}
