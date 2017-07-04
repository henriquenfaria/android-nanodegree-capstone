package com.henriquenfaria.wisetrip.data;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;

import org.zakariya.stickyheaders.SectioningAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExpenseItemHolder extends SectioningAdapter.ItemViewHolder {
    @BindView(R.id.expense_item_layout)
    protected LinearLayout mExpenseTitleLayout;
    @BindView(R.id.expense_title)
    protected TextView mExpenseTitle;

    private OnExpenseItemClickListener mOnExpenseItemClickListener;

    public ExpenseItemHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        mExpenseTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnExpenseItemClickListener != null) {
                    mOnExpenseItemClickListener.onExpenseItemClick(v);
                }
            }
        });
    }

    public void setExpenseTitle(String expenseTitle) {
        mExpenseTitle.setText(expenseTitle);
    }

    public void setOnExpenseItemClickListener(OnExpenseItemClickListener
                                                      onExpenseItemClickListener) {
        mOnExpenseItemClickListener = onExpenseItemClickListener;
    }

    public interface OnExpenseItemClickListener {
        void onExpenseItemClick(View view);
    }
}