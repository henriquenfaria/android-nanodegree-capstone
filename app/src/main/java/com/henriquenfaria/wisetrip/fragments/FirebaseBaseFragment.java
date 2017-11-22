package com.henriquenfaria.wisetrip.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Base Fragment for all Fragments in the app that uses Firebase
 */

public abstract class FirebaseBaseFragment extends BaseFragment {

    protected FirebaseAuth mFirebaseAuth;
    protected FirebaseDatabase mFirebaseDatabase;
    protected DatabaseReference mRootReference;
    protected FirebaseUser mCurrentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mRootReference = mFirebaseDatabase.getReference();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

       // TODO: Check if mCurrentUser is null and call the hosting Activity to finish it
    }
}
