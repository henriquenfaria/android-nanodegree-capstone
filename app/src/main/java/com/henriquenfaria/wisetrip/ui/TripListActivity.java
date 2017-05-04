package com.henriquenfaria.wisetrip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.henriquenfaria.wisetrip.R;

import java.util.ArrayList;
import java.util.List;


/* Main Activity that lists all Trips created by the user */
public class TripListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = TripListActivity.class.getSimpleName();

    // Request code for auth sign in, this is an arbitrary value
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private List<AuthUI.IdpConfig> mAuthProviders;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthProviders = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // User is signed in
                    onSignInInitialize();
                } else {
                    // User is signed out
                    onSignOutCleanup();
                    mAuthProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER)
                            .build());
                    mAuthProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                            .build());
                    startActivityForResult(AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(mAuthProviders)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    // TODO: Delete it if not needed in the future
    private void onSignInInitialize() {
    }

    // TODO: Delete it if not needed in the future
    private void onSignOutCleanup() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirebaseAuthStateListener != null) {
            mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(TripListActivity.this, R.string.toast_successfully_signed_in,
                        Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(TripListActivity.this, R.string.toast_sign_in_canceled,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        // TODO: Implement navigation drawer items
        if (id == R.id.nav_your_trips) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_sign_out) {
            // Sign out
            AuthUI.getInstance().signOut(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
