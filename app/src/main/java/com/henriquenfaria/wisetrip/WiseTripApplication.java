package com.henriquenfaria.wisetrip;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import timber.log.Timber;

public class WiseTripApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //TODO: Create plant for Release
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
