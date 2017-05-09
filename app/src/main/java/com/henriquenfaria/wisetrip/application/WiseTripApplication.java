package com.henriquenfaria.wisetrip.application;


import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class WiseTripApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
