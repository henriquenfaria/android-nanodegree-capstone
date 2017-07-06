package com.henriquenfaria.wisetrip.listeners;

import com.henriquenfaria.wisetrip.models.ExpenseModel;

public interface OnExpenseInteractionListener {
    void onExpenseClicked(ExpenseModel expense);
}