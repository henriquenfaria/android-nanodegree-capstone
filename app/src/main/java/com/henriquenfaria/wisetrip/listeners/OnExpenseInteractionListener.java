package com.henriquenfaria.wisetrip.listeners;

import com.henriquenfaria.wisetrip.models.ExpenseModel;

/**
 * Listener for communication between ExpenseListFragment and its host Activity
 */
public interface OnExpenseInteractionListener {
    void onExpenseClicked(ExpenseModel expense);
}