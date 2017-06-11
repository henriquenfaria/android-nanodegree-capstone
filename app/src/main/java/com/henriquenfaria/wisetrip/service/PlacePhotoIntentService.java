package com.henriquenfaria.wisetrip.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.concurrent.TimeUnit;


public class PlacePhotoIntentService extends IntentService implements GoogleApiClient
        .OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String LOG_TAG = PlacePhotoIntentService.class.getSimpleName();

    public PlacePhotoIntentService() {
        super(PlacePhotoIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
            Trip trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
            if (trip != null) {
                // Retrieves the photo of first trip's destination, save it in the internal
                // storage and update Firebase db with the photo's attributions
                if (intent.getAction().equals(Constants.Action.ACTION_GET_PHOTO)) {
                    GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Places.GEO_DATA_API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();

                    googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

                    if (googleApiClient.isConnected()) {
                        PlacePhotoMetadataResult result = Places.GeoDataApi
                                .getPlacePhotos(googleApiClient, trip.getDestinations().get(0).getId
                                        ()).await();

                        if (result != null && result.getStatus().isSuccess()) {
                            PlacePhotoMetadataBuffer photoMetadataBuffer = result
                                    .getPhotoMetadata();
                            if (photoMetadataBuffer != null && photoMetadataBuffer.getCount() > 0) {

                                PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                                Bitmap image = photo.getPhoto(googleApiClient).await().getBitmap();
                                CharSequence attribution = photo.getAttributions();

                                if (image != null) {
                                    Utils.saveBitmapToInternalStorage(image, Constants.Global
                                            .DESTINATION_PHOTO_DIR, trip.getId());

                                    saveDestinationAttribution(trip, attribution);
                                }

                                photoMetadataBuffer.release();
                            }
                        }
                    }
                    // Deletes from the internal storage the photo of the first trip's destination
                } else if (intent.getAction().equals(Constants.Action.ACTION_DELETE_PHOTO)) {
                    boolean isDeleted = Utils.deleteFileFromInternalStorage(
                            Constants.Global.DESTINATION_PHOTO_DIR, trip.getId());
                    Log.d(LOG_TAG, "photo deleted = " + isDeleted);
                }
            }
        }
    }

    private void saveDestinationAttribution(Trip trip, CharSequence attribution) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userTripReference = firebaseDatabase.getReference()
                .child("user-trips")
                .child(currentUser.getUid());

        DatabaseReference destinationReference = userTripReference
                .child(trip.getId())
                .child("destinations")
                .child("0")
                .child("attribution");
        destinationReference.setValue(attribution);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}
