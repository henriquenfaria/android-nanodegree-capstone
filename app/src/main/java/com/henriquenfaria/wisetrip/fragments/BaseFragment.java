package com.henriquenfaria.wisetrip.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Base Fragment for all Fragments in the app
 */
public class BaseFragment extends Fragment {

    protected FragmentActivity mFragmentActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (mFragmentActivity == null && context instanceof FragmentActivity) {
            mFragmentActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onDetach() {
        mFragmentActivity = null;
        super.onDetach();
    }
}
