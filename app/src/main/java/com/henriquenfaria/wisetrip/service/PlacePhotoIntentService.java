package com.henriquenfaria.wisetrip.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
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

import timber.log.Timber;


public class PlacePhotoIntentService extends IntentService implements GoogleApiClient
        .OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public PlacePhotoIntentService() {
        super(PlacePhotoIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getAction().equals(Constants.Action.ACTION_ADD_PHOTO)) {
            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                Trip trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                    // Retrieves the photo of first trip's destination, save it in the internal
                    // storage and update Firebase db with the photo's attributions
                    if (!Utils.isFileExists(getApplicationContext(),
                            Constants.Global.DESTINATION_PHOTO_DIR, trip.getId())) {
                        boolean updateTripList = intent.getBooleanExtra(Constants.Extra
                                .EXTRA_UPDATE_TRIP_LIST, false);
                        addDestinationPhoto(trip, updateTripList);
                    }
                }
            }
        } else if (intent.getAction().equals(Constants.Action.ACTION_CHANGE_PHOTO)) {
            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                Trip trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                    boolean updateTripList = intent.getBooleanExtra(Constants.Extra
                                    .EXTRA_UPDATE_TRIP_LIST, false);
                    addDestinationPhoto(trip, updateTripList);
                }
            }
        } else if (intent.getAction().equals(Constants.Action.ACTION_REMOVE_PHOTO)) {

            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                Trip trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                    // Deletes from the internal storage the photo of the first trip's destination
                    boolean isDeleted = Utils.deleteFileFromInternalStorage
                            (getApplicationContext(),
                                    Constants.Global.DESTINATION_PHOTO_DIR, trip.getId());
                    if (isDeleted) {
                        Timber.d("destination photo deleted");
                    }
                }
            }

        } else if (intent.getAction().equals(Constants.Action.ACTION_SIGN_OUT_CLEAN_UP)) {
            // Remove local photos and clean Glide's cache and memory
            Utils.deleteFolderFromInternalStorage(getApplicationContext(),
                    Constants.Global.DESTINATION_PHOTO_DIR);
            Glide.get(this).clearDiskCache();
        }
    }

    private void addDestinationPhoto(Trip trip, boolean updateTripList) {
        Timber.d("addDestinationPhoto()");
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
                        Utils.saveBitmapToInternalStorage(getApplicationContext(), image,
                                Constants.Global
                                        .DESTINATION_PHOTO_DIR, trip.getId());

                        addDestinationPhotoAttribution(trip, attribution);

                        if (updateTripList) {
                            sendUpdateTripListBroadcast();
                        }
                    }

                    photoMetadataBuffer.release();
                }
            }
        }
    }

    private void addDestinationPhotoAttribution(Trip trip, CharSequence attribution) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userTripReference = firebaseDatabase.getReference()
                .child("user-trips")
                .child(currentUser.getUid())
                .child(trip.getId());

        DatabaseReference destinationReference = userTripReference
                .child("destinations")
                .child("0")
                .child("attribution");

        destinationReference.setValue(attribution);
    }

    private void sendUpdateTripListBroadcast() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Constants.Action.ACTION_UPDATE_TRIP_LIST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
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
