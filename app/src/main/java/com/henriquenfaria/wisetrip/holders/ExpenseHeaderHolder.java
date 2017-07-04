package com.henriquenfaria.wisetrip.holders;


import android.view.View;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;

import org.zakariya.stickyheaders.SectioningAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExpenseHeaderHolder extends SectioningAdapter.HeaderViewHolder {

    @BindView(R.id.expense_header_title)
    protected TextView mExpenseHeaderTitle;

    public ExpenseHeaderHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setExpenseHeaderTitle(String expenseHeaderTitle) {
        mExpenseHeaderTitle.setText(expenseHeaderTitle);
    }

}