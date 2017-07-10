package com.henriquenfaria.wisetrip.adapters;


import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


// TODO: Remove me if not needed
public class ExpenseAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

    public ExpenseAdapter(List<AbstractFlexibleItem> items, Object listeners) {
        super(items, listeners, true);
    }

}