package com.henriquenfaria.wisetrip.activities;

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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.TripListFragment;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main Activity that lists all user's trips
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG_MAIN_FRAGMENT = "tag_main_fragment";
    @BindView(R.id.fab)
    protected FloatingActionButton mFab;
    private Fragment mFragment;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        // Redirect to Sign In screen if user has not been authenticated
        if (mCurrentUser == null) {
            startActivity(new Intent(this, AuthUiActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setDefaultCurrencyPreferences(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFragment instanceof TripListFragment) {
                    startTripFactory();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            displaySelectedScreen(R.id.nav_your_trips);
        } else {
            mFragment = getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
        }

        configureNavDrawer();
    }

    private void configureNavDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_your_trips);

        View header = navigationView.getHeaderView(0);
        TextView userName = header.findViewById(R.id.nav_user_name);
        userName.setText(mCurrentUser.getDisplayName());
        TextView userEmail = header.findViewById(R.id.nav_user_email);
        userEmail.setText(mCurrentUser.getEmail());
    }

    private void startTripFactory() {
        Intent intent = new Intent(MainActivity.this, TripFactoryActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    private void displaySelectedScreen(int itemId) {
        if (itemId == R.id.nav_your_trips) {
            mFragment = new TripListFragment();
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void setDefaultCurrencyPreferences(Context context) {
        Locale defaultLocale = Locale.getDefault();
        String country = defaultLocale.getCountry();
        if (TextUtils.isEmpty(Utils.getStringFromSharedPrefs(context,
                Constants.Preference.PREFERENCE_LAST_USED_COUNTRY,
                Constants.General.DEFAULT_COUNTRY))) {
            if (!TextUtils.isEmpty(country) && country.length() == 2) {
                Utils.saveStringToSharedPrefs(context,
                        Constants.Preference.PREFERENCE_LAST_USED_COUNTRY, country, false);
                Utils.saveStringToSharedPrefs(context,
                        Constants.Preference.PREFERENCE_LAST_USED_CURRENCY,
                        Utils.getCurrencySymbol(country), false);
            } else {
                Utils.saveStringToSharedPrefs(context,
                        Constants.Preference.PREFERENCE_LAST_USED_COUNTRY,
                        Constants.General.DEFAULT_COUNTRY, false);
                Utils.saveStringToSharedPrefs(context,
                        Constants.Preference.PREFERENCE_LAST_USED_CURRENCY,
                        Constants.General.DEFAULT_CURRENCY, false);
            }
        }
    }
}
