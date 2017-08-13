package com.henriquenfaria.wisetrip.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.android.gms.location.places.Place;

import java.io.Serializable;

public class DestinationModel implements Parcelable, Serializable {

    private String id;
    private String name;
    private String address;
    private String attributions;
    private String phoneNumber;
    private String websiteUri;
    private Long priceLevel;
    private Double rating;
    private LatLngModel latLng;
    private LatLngBoundsModel latLngBounds;


    public DestinationModel() {
        // Required by Firebase
    }

    public DestinationModel(Place place) {
        id = place.getId();
        name = place.getName() != null ? String.valueOf(place.getName()) : "";
        address = place.getAddress() != null ? String.valueOf(place.getAddress()) : "";
        attributions = place.getAttributions() != null ? String.valueOf(place.getAttributions())
                : "";
        phoneNumber = place.getPhoneNumber() != null ? String.valueOf(place.getPhoneNumber()) : "";
        websiteUri = place.getWebsiteUri() != null ? place.getWebsiteUri().toString() : "";
        priceLevel = (long) place.getPriceLevel();
        rating = (double) place.getRating();
        if (place.getLatLng() != null) {
            latLng = new LatLngModel(place.getLatLng());
        }

        if (place.getViewport() != null) {
            latLngBounds = new LatLngBoundsModel((place.getViewport()));
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLngModel getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLngModel latLng) {
        this.latLng = latLng;
    }

    @Nullable
    public LatLngBoundsModel getLatLngBounds() {
        return latLngBounds;
    }

    public void setLatLngBounds(LatLngBoundsModel latLngBounds) {
        this.latLngBounds = latLngBounds;
    }

    @Nullable
    public String getAttributions() {
        return attributions;
    }

    public void setAttributions(String attributions) {
        this.attributions = attributions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeString(this.phoneNumber);
        dest.writeValue(this.priceLevel);
        dest.writeValue(this.rating);
        dest.writeString(this.websiteUri);
        dest.writeParcelable(this.latLng, flags);
        dest.writeParcelable(this.latLngBounds, flags);
        dest.writeString(this.attributions);
    }

    protected DestinationModel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.address = in.readString();
        this.phoneNumber = in.readString();
        this.priceLevel = (Long) in.readValue(Long.class.getClassLoader());
        this.rating = (Double) in.readValue(Double.class.getClassLoader());
        this.websiteUri = in.readString();
        this.latLng = in.readParcelable(LatLngModel.class.getClassLoader());
        this.latLngBounds = in.readParcelable(LatLngBoundsModel.class.getClassLoader());
        this.attributions = in.readString();
    }

    public static final Creator<DestinationModel> CREATOR = new Creator<DestinationModel>() {
        @Override
        public DestinationModel createFromParcel(Parcel source) {
            return new DestinationModel(source);
        }

        @Override
        public DestinationModel[] newArray(int size) {
            return new DestinationModel[size];
        }
    };
}
