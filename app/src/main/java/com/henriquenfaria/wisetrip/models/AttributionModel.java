package com.henriquenfaria.wisetrip.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AttributionModel implements Parcelable {

    // TripModel or PlaceModel id
    private String id;
    private String text;

    public AttributionModel() {
        // Required for Firebase
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.text);
    }

    protected AttributionModel(Parcel in) {
        this.id = in.readString();
        this.text = in.readString();
    }

    public static final Parcelable.Creator<AttributionModel> CREATOR = new Parcelable
            .Creator<AttributionModel>() {
        @Override
        public AttributionModel createFromParcel(Parcel source) {
            return new AttributionModel(source);
        }

        @Override
        public AttributionModel[] newArray(int size) {
            return new AttributionModel[size];
        }
    };
}
