package com.henriquenfaria.wisetrip.utils;

import android.util.SparseArray;

import java.util.ArrayList;

/* Utility class with static helper methods */
public class Utils {

    public static <C> ArrayList<C> sparseArrayAsArrayList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        ArrayList<C> arrayList = new ArrayList<>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }
}
