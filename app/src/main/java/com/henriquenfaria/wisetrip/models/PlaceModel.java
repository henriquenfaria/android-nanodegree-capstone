package com.henriquenfaria.wisetrip.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class PlaceModel implements Parcelable, Serializable {

    private String id;
    private String name;
    private Long date;
    private DestinationModel destination;

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

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public DestinationModel getDestination() {
        return destination;
    }

    public void setDestination(DestinationModel destination) {
        this.destination = destination;
    }

    public PlaceModel() {
        id = "";
        name = "";
        date = -1L;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeValue(this.date);
        dest.writeParcelable(this.destination, flags);
    }

    protected PlaceModel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.date = (Long) in.readValue(Long.class.getClassLoader());
        this.destination = in.readParcelable(DestinationModel.class.getClassLoader());
    }

    public static final Creator<PlaceModel> CREATOR = new Creator<PlaceModel>() {
        @Override
        public PlaceModel createFromParcel(Parcel source) {
            return new PlaceModel(source);
        }

        @Override
        public PlaceModel[] newArray(int size) {
            return new PlaceModel[size];
        }
    };
}
