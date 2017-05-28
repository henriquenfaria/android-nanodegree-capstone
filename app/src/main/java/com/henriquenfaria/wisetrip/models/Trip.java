package com.henriquenfaria.wisetrip.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Trip implements Parcelable {

    private String title;
    private long startDate;
    private long endDate;
    private List<Destination> destinations;

    public Trip(String title, long startDate, long endDate, List<Destination> destinations) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destinations = destinations;
    }

    public Trip() {
        // Required for Firebase
    }

    //TODO: Is @Exclude really needed here?
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("destinations", destinations);
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeLong(this.startDate);
        dest.writeLong(this.endDate);
        dest.writeList(this.destinations);
    }

    protected Trip(Parcel in) {
        this.title = in.readString();
        this.startDate = in.readLong();
        this.endDate = in.readLong();
        this.destinations = new ArrayList<>();
        in.readList(this.destinations, Destination.class.getClassLoader());
    }

    public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel source) {
            return new Trip(source);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };
}


