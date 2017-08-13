package com.henriquenfaria.wisetrip.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class HeaderModel implements Serializable, Parcelable {

    public static final Parcelable.Creator<HeaderModel> CREATOR = new Parcelable
            .Creator<HeaderModel>() {
        @Override
        public HeaderModel createFromParcel(Parcel source) {
            return new HeaderModel(source);
        }

        @Override
        public HeaderModel[] newArray(int size) {
            return new HeaderModel[size];
        }
    };
    private Long id;
    private String title;

    public HeaderModel() {
    }

    protected HeaderModel(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.title = in.readString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HeaderModel) {
            HeaderModel inItem = (HeaderModel) obj;
            return id.equals(inItem.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", title=" + title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.title);
    }
}
