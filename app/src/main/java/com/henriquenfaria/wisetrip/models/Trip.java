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
    private Long startDate;
    private Long endDate;
    private Map<String, Traveler> travelers;
    private List<Destination> destinations;

    public Trip(String title, long startDate, long endDate,  Map<String,Traveler> travelers, List<Destination>
            destinations) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.travelers = travelers;
        this.destinations = destinations;
    }

    public Trip() {
        // Required for Firebase

        title = "";
        startDate = -1L;
        endDate = -1L;
        travelers = new HashMap<>();
        destinations = new ArrayList<>();
    }

    //TODO: Is @Exclude really needed here?
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("travelers", travelers);
        result.put("destinations", destinations);
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Map<String, Traveler> getTravelers() {
        return travelers;
    }

    public void setTravelers(Map<String, Traveler> travelers) {
        this.travelers = travelers;
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
        dest.writeValue(this.startDate);
        dest.writeValue(this.endDate);
        dest.writeInt(this.travelers.size());
        for (Map.Entry<String, Traveler> entry : this.travelers.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
        dest.writeTypedList(this.destinations);
    }

    protected Trip(Parcel in) {
        this.title = in.readString();
        this.startDate = (Long) in.readValue(Long.class.getClassLoader());
        this.endDate = (Long) in.readValue(Long.class.getClassLoader());
        int travelersSize = in.readInt();
        this.travelers = new HashMap<String, Traveler>(travelersSize);
        for (int i = 0; i < travelersSize; i++) {
            String key = in.readString();
            Traveler value = in.readParcelable(Traveler.class.getClassLoader());
            this.travelers.put(key, value);
        }
        this.destinations = in.createTypedArrayList(Destination.CREATOR);
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
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


