package com.henriquenfaria.wisetrip.flexibles;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.mukesh.countrypicker.Country;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.viewholders.FlexibleViewHolder;
import timber.log.Timber;

/**
 * Flexible for Budget item
 */
public class BudgetItem extends AbstractFlexibleItem<BudgetItem.ItemViewHolder>
        implements IFilterable, IHolder<BudgetModel> {

    private BudgetModel mBudget;

    public BudgetItem(BudgetModel budget) {
        this.mBudget = budget;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BudgetItem) {
            BudgetItem inItem = (BudgetItem) o;
            return mBudget.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mBudget.hashCode();
    }


    public BudgetModel getModel() {
        return mBudget;
    }

    @Override
    public boolean filter(String constraint) {
        return mBudget.getTitle() != null
                && mBudget.getTitle().toLowerCase().trim().contains(constraint);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.budget_item;
    }

    @Override
    public ItemViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ItemViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, ItemViewHolder holder,
                               int position, List payloads) {

        holder.mTitle.setText(mBudget.getTitle());

        Currency currency;
        try {
            currency = Currency.getInstance(mBudget.getCurrency());
        } catch (IllegalArgumentException | NullPointerException e) {
            Timber.e("Exception while getting Currency instance");
            e.printStackTrace();
            currency = Currency.getInstance(Constants.General.DEFAULT_CURRENCY);
        }

        DecimalFormat decimalFormat = new DecimalFormat(" ¤ ###,###,##0.00");
        decimalFormat.setCurrency(currency);

        try {
            String formattedTotalAmount = decimalFormat.format(mBudget.getBudgetAmount());
            holder.mTotalAmount.setText(formattedTotalAmount);
        } catch (IllegalArgumentException e) {
            Timber.e("Exception while formatting total budget amount");
            e.printStackTrace();
            holder.mTotalAmount.setText("");
        }

        try {
            String formattedRemainingAmount = decimalFormat.format(
                    mBudget.getBudgetAmount() - mBudget.getExpensesAmount());
            holder.mRemainingAmount.setText(formattedRemainingAmount);
        } catch (IllegalArgumentException e) {
            Timber.e("Exception while formatting total budget amount");
            e.printStackTrace();
            holder.mRemainingAmount.setText("");
        }

        Country country = new Country();
        country.setCode(mBudget.getCountry());
        country.loadFlagByCode(holder.mCurrencyIcon.getContext());
        holder.mCurrencyIcon.setImageResource(country.getFlag());
    }

    static class ItemViewHolder extends FlexibleViewHolder {

        @BindView(R.id.budget_card)
        public CardView mBudgetCard;

        @BindView(R.id.budget_title)
        public TextView mTitle;

        @BindView(R.id.budget_amount)
        public TextView mTotalAmount;

        @BindView(R.id.remaining_amount)
        public TextView mRemainingAmount;

        @BindView(R.id.currency_icon)
        public ImageView mCurrencyIcon;

        public ItemViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            ButterKnife.bind(this, view);
            mBudgetCard.setOnClickListener(new View.OnClickListener() {
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