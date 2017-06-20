package com.henriquenfaria.wisetrip.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.henriquenfaria.wisetrip.BuildConfig;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.service.PlacePhotoIntentService;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.util.Arrays;

/* Activity to handle user authentication */
public class AuthUiActivity extends AppCompatActivity {

    // Request code for auth sign in, this is an arbitrary value
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();

        // Redirect to Trip List screen if user has been authenticated
        FirebaseAuth auth = mFirebaseAuth;
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    // User is signed in
                    onSignInInitialize();
                    startActivity(new Intent(AuthUiActivity.this, MainActivity.class));
                    finish();
                } else {
                    // User is signed out
                    onSignOutCleanup();
                    startActivityForResult(AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER)
                                                    .build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                                                    .build()))
                                    .setLogo(R.drawable.wise_trip_logo)
                                    .setTheme(R.style.AppTheme)
                                    .build(),
                            RC_SIGN_IN);
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
    }

    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == ResultCodes.OK) {
            // Successfully signed in
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        } else if (resultCode == RESULT_CANCELED) {
            //User pressed back button
            finish();
            return;
        } else {
            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                Toast.makeText(AuthUiActivity.this, R.string.no_internet_connection,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                Toast.makeText(AuthUiActivity.this, R.string.unknown_error,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(AuthUiActivity.this, R.string.unknown_error,
                Toast.LENGTH_SHORT).show();
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

    private void onSignInInitialize() {
    }

    private void onSignOutCleanup() {
        // Start photo's clean up task
        Intent placePhotoIntentService = new Intent(this, PlacePhotoIntentService.class);
        placePhotoIntentService.setAction(Constants.Action.ACTION_SIGN_OUT_CLEAN_UP);
        startService(placePhotoIntentService);
    }
}
