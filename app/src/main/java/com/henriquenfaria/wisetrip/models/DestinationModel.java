package com.henriquenfaria.wisetrip.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;

import java.io.Serializable;

public class DestinationModel implements Parcelable, Serializable {

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

    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String attribution;

    public DestinationModel() {
        // Required for Firebase
    }

    public DestinationModel(Place place) {
        id = place.getId();
        name = String.valueOf(place.getName());
        if (place.getLatLng() != null) {
            latitude = place.getLatLng().latitude;
            longitude = place.getLatLng().longitude;
        } else {
            // Undefined
            latitude = -1;
            longitude = -1;
        }
        attribution = "";
    }

    protected DestinationModel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.attribution = in.readString();
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.attribution);
    }
}
