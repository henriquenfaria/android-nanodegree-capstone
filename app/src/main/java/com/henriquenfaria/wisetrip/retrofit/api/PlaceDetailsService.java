package com.henriquenfaria.wisetrip.retrofit.api;

import com.henriquenfaria.wisetrip.retrofit.models.PlaceDetailsResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface responsible for detailing the GET's URL
 */
public interface PlaceDetailsService {
    String BASE_URL = "https://maps.googleapis.com";

    @GET("/maps/api/place/details/json")
    Call<PlaceDetailsResult> getPlaceDetailsResult(@Query("placeid") String placeid, @Query("key") String key);

    @GET("/maps/api/place/photo")
    Call<PlaceDetailsResult> getPhotoResult(@Query("photoreference") String photoreference, @Query("maxheight") String maxheight, @Query("key") String key);
}