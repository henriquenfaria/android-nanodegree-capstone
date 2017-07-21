package com.henriquenfaria.wisetrip.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.TripListFragment;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/* Main Activity that lists all user's trips */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG_MAIN_FRAGMENT = "tag_main_fragment";
    @BindView(R.id.fab)
    protected FloatingActionButton mFab;
    private boolean mIsTwoPane;
    private Fragment mFragment;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTripsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        // Redirect to Sign In screen if user has not been authenticated
        if (mCurrentUser == null) {
            startActivity(new Intent(this, AuthUiActivity.class));
            finish();
            return;
        }

        mTripsReference = mFirebaseDatabase.getReference()
                .child("trips")
                .child(mCurrentUser.getUid());

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setDefaultCurrencyPreferences(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFragment instanceof TripListFragment) {
                    startTripFactory();
                }
            }
        });

        DrawerLayout drawer = ButterKnife.findById(this, R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        //TODO: Deprecated
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = ButterKnife.findById(this, R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*TODO: Gonna use master-detail pattern?
         Seems this is not useful for users, since they will not be constantly switching trips */
        if (ButterKnife.findById(this, R.id.detail_fragment_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mIsTwoPane = true;
        }

        if (savedInstanceState == null) {
            displaySelectedScreen(R.id.nav_your_trips);
        } else {
            mFragment = getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
        }

        navigationView.setCheckedItem(R.id.nav_your_trips);
    }

    private void startTripFactory() {
        Intent intent = new Intent(MainActivity.this, TripFactoryActivity.class);
        startActivityForResult(intent, Constants.Request.REQUEST_TRIP_FACTORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.Request.REQUEST_TRIP_FACTORY
                && resultCode != Activity.RESULT_CANCELED) {
            handleRequestTripFactory(resultCode, data);
        }
    }

    private void handleRequestTripFactory(int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        final TripModel trip = data.getParcelableExtra(Constants.Extra.EXTRA_TRIP);

        if (resultCode == Constants.Result.RESULT_TRIP_ADDED && trip != null) {
            DatabaseReference databaseReference = mTripsReference.push();
            trip.setId(databaseReference.getKey());
            databaseReference.setValue(trip);

        } else if (resultCode == Constants.Result.RESULT_TRIP_CHANGED
                && trip != null && !TextUtils.isEmpty(trip.getId())) {
            DatabaseReference databaseReference = mTripsReference.child(trip.getId());
            databaseReference.setValue(trip);

        } else if (resultCode == Constants.Result.RESULT_TRIP_REMOVED
                && trip != null && !TextUtils.isEmpty(trip.getId())) {
            mTripsReference.child(trip.getId()).removeValue();
            removeOtherTripData(trip);

        } else if (resultCode == Constants.Result.RESULT_TRIP_ERROR) {
            Toast.makeText(this, getString(R.string.trip_updated_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Remove trip attributions, expenses, budgets and places
    private void removeOtherTripData(TripModel trip) {
        // Remove Trip attributions
        DatabaseReference attributionsReference = mFirebaseDatabase.getReference()
                .child("attributions")
                .child(mCurrentUser.getUid())
                .child(trip.getId());
        attributionsReference.removeValue();

        // Remove Trip expenses
        DatabaseReference expensesReference = mFirebaseDatabase.getReference()
                .child("expenses")
                .child(mCurrentUser.getUid())
                .child(trip.getId());
        expensesReference.removeValue();

        // Remove Trip budgets
        DatabaseReference budgetsReference = mFirebaseDatabase.getReference()
                .child("budgets")
                .child(mCurrentUser.getUid())
                .child(trip.getId());
        budgetsReference.removeValue();

        // Remove Trip places
        //TODO: Uncomment this after places implementation
        /* DatabaseReference placesReference = mFirebaseDatabase.getReference()
                .child("attributions")
                .child(mCurrentUser.getUid())
                .child(trip.getId());
        placesReference.removeValue();*/
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = ButterKnife.findById(this, R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    private void displaySelectedScreen(int itemId) {
        if (itemId == R.id.nav_your_trips) {
            mFragment = new TripListFragment();
        } else if (itemId == R.id.nav_settings) {
            // TODO: Implement Settings navigation
        } else if (itemId == R.id.nav_sign_out) {
            // Sign out
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // user is now signed out
                                startActivity(new Intent(MainActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, R.string
                                        .sign_out_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        if (mFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, mFragment, TAG_MAIN_FRAGMENT).commit();

        }

        DrawerLayout drawer = ButterKnife.findById(this, R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void setDefaultCurrencyPreferences(Context context) {
        Locale defaultLocale = Locale.getDefault();
        String country = defaultLocale.getCountry();
        if (TextUtils.isEmpty(Utils.getStringFromSharedPrefs(context,
                Constants.Preference.PREFERENCE_DEFAULT_COUNTRY))) {
            if (!TextUtils.isEmpty(country) && country.length() == 2) {
                Utils.saveStringToSharedPrefs(context,
                        Constants.Preference.PREFERENCE_DEFAULT_COUNTRY, country, false);
                Utils.saveStringToSharedPrefs(context,
                        Constants.Preference.PREFERENCE_DEFAULT_CURRENCY,
                        Utils.getCurrencySymbol(country), false);
            } else {
                Utils.saveStringToSharedPrefs(context,
                        Constants.Preference.PREFERENCE_DEFAULT_COUNTRY,
                        Constants.General.DEFAULT_COUNTRY, false);
                Utils.saveStringToSharedPrefs(context,
                        Constants.Preference.PREFERENCE_DEFAULT_CURRENCY,
                        Constants.General.DEFAULT_CURRENCY, false);
            }
        }
    }
}
