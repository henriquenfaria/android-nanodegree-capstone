package com.henriquenfaria.wisetrip.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Trip implements Parcelable, Comparable<Trip> {

    private String id;
    private String title;
    private Long startDate;
    private Long endDate;
    private Map<String, Traveler> travelers;
    private List<Destination> destinations;



    public enum State {
        CURRENT, UPCOMING, PAST
    }

    public Trip() {
        // Required for Firebase
        id = "";
        title = "";
        startDate = -1L;
        endDate = -1L;
        travelers = new HashMap<>();
        destinations = new ArrayList<>();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("travelers", travelers);
        result.put("destinations", destinations);
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public State getState(long currentMillis){
        if (currentMillis >= startDate && currentMillis <= endDate) {
            return State.CURRENT;
        } else if (endDate < currentMillis) {
            return State.PAST;
        } else {
            return State.UPCOMING;
        }
    }

   /* @Override
    public int compare(Trip trip1, Trip trip2) {
        // Ascending order using start date
        return  (int) (trip1.getStartDate() - trip2.getStartDate());
    }*/

    @Override
    public int compareTo(@NonNull Trip trip) {
        // Ascending order using start date
        return (int) (this.getStartDate() - trip.getStartDate());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
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
        this.id = in.readString();
        this.title = in.readString();
        this.startDate = (Long) in.readValue(Long.class.getClassLoader());
        this.endDate = (Long) in.readValue(Long.class.getClassLoader());
        int travelersSize = in.readInt();
        this.travelers = new HashMap<>(travelersSize);
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


