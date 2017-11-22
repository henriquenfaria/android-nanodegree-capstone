package com.henriquenfaria.wisetrip.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.models.AttributionModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * IntentService implementation that handles Places API communication in the background.
 * It is responsible to fetch, store, update and delete trip photos from the device local storage.
 */
public class PlacePhotoIntentService extends IntentService implements GoogleApiClient
        .OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public PlacePhotoIntentService() {
        super(PlacePhotoIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }

        if (TextUtils.equals(intent.getAction(), Constants.Action.ACTION_ADD_PHOTO)) {
            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                    // Retrieves the photo of first trip's destination, save it in the internal
                    // storage and update Firebase db with the photo's attributions
                    if (!Utils.isFileExists(getApplicationContext(),
                            Constants.General.DESTINATION_PHOTO_DIR, trip.getId())) {
                        addDestinationPhoto(trip, true);
                    }
                }
            }
        } else if (TextUtils.equals(intent.getAction(), Constants.Action.ACTION_CHANGE_PHOTO)) {
            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                    addDestinationPhoto(trip, true);
                }
            }
        } else if (TextUtils.equals(intent.getAction(), Constants.Action.ACTION_REMOVE_PHOTO)) {
            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                if (trip != null && !TextUtils.isEmpty(trip.getId())) {
                    // Deletes from the internal storage the photo of the first trip's destination
                    boolean isDeleted = Utils.deleteFileFromInternalStorage
                            (getApplicationContext(),
                                    Constants.General.DESTINATION_PHOTO_DIR, trip.getId());
                    if (isDeleted) {
                        Timber.d("destination photo deleted");
                    }
                }
            }
        } else if (TextUtils.equals(intent.getAction(),
                Constants.Action.ACTION_SIGN_OUT_CLEAN_UP)) {
            // Remove local photos
            Utils.deleteFolderFromInternalStorage(getApplicationContext(),
                    Constants.General.DESTINATION_PHOTO_DIR);

            // Glide's clearDiskCache must be called in a background thread
            Glide.get(getApplicationContext()).clearDiskCache();
        }
    }

    private void addDestinationPhoto(TripModel trip, boolean updateTripList) {
        Timber.d("addDestinationPhoto()");
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (googleApiClient.isConnected()) {
            if (trip.getDestinations().size() > 0) {
                PlacePhotoMetadataResult result = Places.GeoDataApi
                        .getPlacePhotos(googleApiClient, trip.getDestinations().get(0).getId())
                        .await();

                if (result.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                    if (photoMetadataBuffer != null && photoMetadataBuffer.getCount() > 0) {
                        PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                        Bitmap image = photo.getPhoto(googleApiClient).await().getBitmap();
                        CharSequence attribution = photo.getAttributions();

                        if (image != null) {
                            Utils.saveBitmapToInternalStorage(getApplicationContext(), image,
                                    Constants.General.DESTINATION_PHOTO_DIR, trip.getId());

                            addDestinationPhotoAttribution(trip, attribution);

                            // Update widget photos and attributions
                            Utils.updateAppWidgets(getApplicationContext(), trip.getId(), false);

                            if (updateTripList) {
                                sendUpdateTripListBroadcast(trip);
                            }
                        }

                        photoMetadataBuffer.release();
                    }
                }
            }
        }
    }

    private void addDestinationPhotoAttribution(TripModel trip, CharSequence attributionText) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference attributionsReference = firebaseDatabase.getReference()
                    .child(FirebaseDbContract.Attributions.PATH_ATTRIBUTIONS)
                    .child(currentUser.getUid())
                    .child(trip.getId());

            AttributionModel attribution = new AttributionModel();
            attribution.setId(attributionsReference.getKey());
            attribution.setText(attributionText != null ? attributionText.toString() : "");
            attributionsReference.setValue(attribution);
        }
    }

    private void sendUpdateTripListBroadcast(TripModel trip) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) trip);
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
