package com.henriquenfaria.wisetrip.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Attribution implements Parcelable {

    // Trip or Place id
    private String id;
    private String text;

    public Attribution() {
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

    protected Attribution(Parcel in) {
        this.id = in.readString();
        this.text = in.readString();
    }

    public static final Parcelable.Creator<Attribution> CREATOR = new Parcelable
            .Creator<Attribution>() {
        @Override
        public Attribution createFromParcel(Parcel source) {
            return new Attribution(source);
        }

        @Override
        public Attribution[] newArray(int size) {
            return new Attribution[size];
        }
    };
}
