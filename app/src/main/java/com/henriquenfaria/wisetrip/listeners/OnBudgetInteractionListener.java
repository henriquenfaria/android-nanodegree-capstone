package com.henriquenfaria.wisetrip.listeners;

import com.henriquenfaria.wisetrip.models.BudgetModel;

/**
 * Listener for communication between BudgetListFragment and its host Activity
 */
public interface OnBudgetInteractionListener {
    void onBudgetClicked(BudgetModel budget);
}