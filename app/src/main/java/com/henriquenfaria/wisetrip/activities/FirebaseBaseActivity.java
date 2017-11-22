package com.henriquenfaria.wisetrip.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;

import timber.log.Timber;

/**
 * Base Activity for all Fragments in the app that uses Firebase
 */

public abstract class FirebaseBaseActivity extends AppCompatActivity {

    protected FirebaseAuth mFirebaseAuth;
    protected FirebaseDatabase mFirebaseDatabase;
    protected DatabaseReference mRootReference;
    protected FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_factory);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRootReference = mFirebaseDatabase.getReference();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        if (mCurrentUser == null) {
            Timber.d("Finishing Activity prematurely due to null Current User");
            finish();
        }
    }
}
