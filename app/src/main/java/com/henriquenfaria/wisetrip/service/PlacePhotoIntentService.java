package com.henriquenfaria.wisetrip.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.BuildConfig;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.retrofit.api.PlaceDetailsService;
import com.henriquenfaria.wisetrip.retrofit.models.Photo;
import com.henriquenfaria.wisetrip.retrofit.models.PlaceDetailsResult;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.henriquenfaria.wisetrip.BuildConfig.GOOGLE_GEO_API_ANDROID_KEY;

public class PlacePhotoIntentService extends IntentService {

    private static final String LOG_TAG = PlacePhotoIntentService.class.getSimpleName();

    public PlacePhotoIntentService() {
        super(PlacePhotoIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null && intent.hasExtra(Constants.Extras.EXTRA_TRIP)) {
            Trip trip = intent.getParcelableExtra(Constants.Extras.EXTRA_TRIP);
            if (trip != null) {

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference userTripReference = firebaseDatabase.getReference()
                        .child("user-trips")
                        .child(currentUser.getUid());

                try {
                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

                    //TODO: Must validate if this log is not being printed in production builds
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                        httpClient.addInterceptor(logging);
                    }

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(PlaceDetailsService.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(httpClient.build())
                            .build();

                    String placeId = trip.getDestinations().get(0).getId();

                    PlaceDetailsService service = retrofit.create(PlaceDetailsService.class);
                    Call<PlaceDetailsResult> call = service.getPlaceDetailsResult(placeId,
                            GOOGLE_GEO_API_ANDROID_KEY);

                    Response<PlaceDetailsResult> response = call.execute();
                    PlaceDetailsResult responseDetailsResult = response.body();

                    //TODO: Must save and display responseDetailsResult.getHtmlAttributions()
                    if (responseDetailsResult.getResult() != null) {
                        List<Photo> photos = responseDetailsResult.getResult().getPhotos();
                        if (photos != null && photos.size() > 0) {
                            String photoReference = photos.get(0).getPhotoReference();
                            if (!TextUtils.isEmpty(photoReference)) {
                                DatabaseReference destinationReference = userTripReference
                                        .child(trip.getId())
                                        .child("destinations")
                                        .child("0")
                                        .child("photoReference");
                                destinationReference.setValue(photoReference);
                            }
                        }
                    }
                } catch (IOException ex) {
                    Log.d(LOG_TAG, ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }
}
