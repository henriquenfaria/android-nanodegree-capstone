package com.henriquenfaria.wisetrip.flexibles;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.viewholders.FlexibleViewHolder;
import timber.log.Timber;

public class ExpenseItem extends AbstractSectionableItem<ExpenseItem.ItemViewHolder, ExpenseHeader>
        implements IFilterable, IHolder<ExpenseModel> {

    private ExpenseModel mExpense;

    public ExpenseItem(ExpenseModel expense, ExpenseHeader header) {
        super(header);
        this.mExpense = expense;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExpenseItem) {
            ExpenseItem inItem = (ExpenseItem) o;
            return mExpense.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mExpense.hashCode();
    }


    public ExpenseModel getModel() {
        return mExpense;
    }

    @Override
    public boolean filter(String constraint) {
        return mExpense.getTitle() != null
                && mExpense.getTitle().toLowerCase().trim().contains(constraint);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.expense_item;
    }

    @Override
    public ItemViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ItemViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, ItemViewHolder holder,
                               int position, List payloads) {
        holder.mTitle.setText(mExpense.getTitle());

        Currency currency;
        try {
            currency = Currency.getInstance(mExpense.getCurrency());
        } catch (IllegalArgumentException| NullPointerException e) {
            Timber.e("Exception while getting Currency instance");
            e.printStackTrace();
            currency = Currency.getInstance(Constants.General.DEFAULT_CURRENCY);
        }

        DecimalFormat decimalFormat = new DecimalFormat("¤ ###,###,###.00");
        decimalFormat.setCurrency(currency);
        String formattedAmount = decimalFormat.format(mExpense.getAmount());
        holder.mAmount.setText(formattedAmount);
    }

    static class ItemViewHolder extends FlexibleViewHolder {

        @BindView(R.id.expense_item_layout)
        public RelativeLayout mLayout;

        @BindView(R.id.expense_title)
        public TextView mTitle;

        @BindView(R.id.expense_amount)
        public TextView mAmount;


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