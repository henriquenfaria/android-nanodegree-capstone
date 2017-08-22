package com.henriquenfaria.wisetrip.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.henriquenfaria.wisetrip.BuildConfig;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.services.PlacePhotoIntentService;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/* Activity to handle user authentication */
public class AuthUiActivity extends AppCompatActivity {

    // Request code for auth sign in, this is an arbitrary value
    private static final int RC_SIGN_IN = 1;
    @BindView(R.id.root)
    protected RelativeLayout mRootLayout;
    @BindView(R.id.logo_image)
    protected ImageView mLogoImageView;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private boolean mAuthStateListenerCalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        // Redirect to TripModel List screen if user has been authenticated
        FirebaseAuth auth = mFirebaseAuth;
        if (auth.getCurrentUser() != null) {
            mLogoImageView.setVisibility(View.GONE);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (!mAuthStateListenerCalled) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    mAuthStateListenerCalled = true;
                    if (auth.getCurrentUser() != null) {
                        // User is signed in
                        onSignInInitialize();
                        mLogoImageView.setVisibility(View.GONE);
                        startActivity(new Intent(AuthUiActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // User is signed out
                        onSignOutCleanup();
                        startActivityForResult(AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                                                        .build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER)
                                                        .build()))
                                        .setLogo(R.drawable.wise_trip_logo)
                                        .setTheme(R.style.AppTheme)
                                        .build(),
                                RC_SIGN_IN);
                    }
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        Toast.makeText(AuthUiActivity.this, R.string.unknown_error,
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleSignInResponse(int resultCode, Intent data) {
        Timber.d("handleSignInResponse");
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == ResultCodes.OK) {
            // Successfully signed in
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        } else if (resultCode == ResultCodes.CANCELED) {
            if (response != null) {
                switch (response.getErrorCode()) {
                    case ErrorCodes.NO_NETWORK:
                        mLogoImageView.setVisibility(View.VISIBLE);
                        displayErrorSnackBar(R.string.no_internet_connection);
                        return;

                    case ErrorCodes.UNKNOWN_ERROR:
                        mLogoImageView.setVisibility(View.VISIBLE);
                        displayErrorSnackBar(R.string.unknown_error);
                        return;
                    default:
                        Toast.makeText(AuthUiActivity.this, R.string.unknown_error, Toast
                                .LENGTH_SHORT)
                                .show();
                        break;
                }
            }
        }
        finish();
    }


    private void displayErrorSnackBar(int msg) {
        Snackbar snackbar = Snackbar
                .make(mRootLayout, getString(msg), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mFirebaseAuthStateListener != null) {
                            mAuthStateListenerCalled = false;
                            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
                            mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
                        }
                    }
                });
        snackbar.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirebaseAuthStateListener != null) {
            mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
        }

    }

    private void onSignInInitialize() {
        // Nothing to initialize yet
    }

    private void onSignOutCleanup() {
        // Start photo's clean up task
        Intent placePhotoIntentService = new Intent(this, PlacePhotoIntentService.class);
        placePhotoIntentService.setAction(Constants.Action.ACTION_SIGN_OUT_CLEAN_UP);
        startService(placePhotoIntentService);

        // Sign out all widgets
        Utils.signOutAppWidgets(this);
    }
}
