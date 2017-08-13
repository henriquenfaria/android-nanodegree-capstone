package com.henriquenfaria.wisetrip.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class LatLngBoundsModel implements Parcelable {
    private LatLngModel southwest;
    private LatLngModel northeast;

    public LatLngBoundsModel() {
        // Required by Firebase
    }

    public LatLngBoundsModel(LatLngBounds latLngBounds) {
        southwest = new LatLngModel(latLngBounds.southwest);
        northeast = new LatLngModel(latLngBounds.northeast);
    }

    public LatLngBoundsModel(LatLng southwest, LatLng northeast) {
        this.southwest = new LatLngModel(southwest);
        this.northeast = new LatLngModel(northeast);
    }

    public LatLngModel getSouthwest() {
        return southwest;
    }

    public void setSouthwest(LatLngModel southwest) {
        this.southwest = southwest;
    }

    public LatLngModel getNortheast() {
        return northeast;
    }

    public void setNortheast(LatLngModel northeast) {
        this.northeast = northeast;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.southwest, flags);
        dest.writeParcelable(this.northeast, flags);
    }

    protected LatLngBoundsModel(Parcel in) {
        this.southwest = in.readParcelable(LatLng.class.getClassLoader());
        this.northeast = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Parcelable.Creator<LatLngBoundsModel> CREATOR = new Parcelable
            .Creator<LatLngBoundsModel>() {
        @Override
        public LatLngBoundsModel createFromParcel(Parcel source) {
            return new LatLngBoundsModel(source);
        }

        @Override
        public LatLngBoundsModel[] newArray(int size) {
            return new LatLngBoundsModel[size];
        }
    };
}
