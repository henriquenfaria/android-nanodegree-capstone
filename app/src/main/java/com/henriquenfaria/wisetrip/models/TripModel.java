package com.henriquenfaria.wisetrip.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class TripModel implements Parcelable, Serializable, Comparable<TripModel> {

    public static final Creator<TripModel> CREATOR = new Creator<TripModel>() {
        @Override
        public TripModel createFromParcel(Parcel source) {
            return new TripModel(source);
        }

        @Override
        public TripModel[] newArray(int size) {
            return new TripModel[size];
        }
    };

    private String id;
    private String title;
    private Long startDate;
    private Long endDate;
    private Map<String, TravelerModel> travelers;

    //TODO: Replace it with a Map?
    private List<DestinationModel> destinations;

    public TripModel() {
        // Required for Firebase
        id = "";
        title = "";
        startDate = -1L;
        endDate = -1L;
        travelers = new HashMap<>();
        destinations = new ArrayList<>();
    }

    protected TripModel(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.startDate = (Long) in.readValue(Long.class.getClassLoader());
        this.endDate = (Long) in.readValue(Long.class.getClassLoader());
        int travelersSize = in.readInt();
        this.travelers = new HashMap<>(travelersSize);
        for (int i = 0; i < travelersSize; i++) {
            String key = in.readString();
            TravelerModel value = in.readParcelable(TravelerModel.class.getClassLoader());
            this.travelers.put(key, value);
        }
        this.destinations = in.createTypedArrayList(DestinationModel.CREATOR);
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

    public Map<String, TravelerModel> getTravelers() {
        return travelers;
    }

    public void setTravelers(Map<String, TravelerModel> travelers) {
        this.travelers = travelers;
    }

    public List<DestinationModel> getDestinations() {
        return destinations;
    }

    public State getState(long currentMillis) {
        if (currentMillis >= this.startDate && currentMillis <= (this.endDate + Constants.General
                .DAY_IN_MILLIS)) {
            return State.CURRENT;
        } else if (this.endDate < currentMillis) {
            return State.PAST;
        } else {
            return State.UPCOMING;
        }
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
        for (Map.Entry<String, TravelerModel> entry : this.travelers.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
        dest.writeTypedList(this.destinations);
    }

    @Override
    public int compareTo(@NonNull TripModel trip) {
        return this.getStartDate().compareTo(trip.getStartDate());
    }

    @Override
    public int hashCode() {
         /* We only need to use id to compare if a TripModel is the same. Since id is unique,
          we can ignore the other properties. It will increase the performance*/
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TripModel) {
               /* We only need to use id to compare if a TripModel is the same. Since id is unique,
            we can ignore the other properties. It will increase the performance*/
            TripModel trip = (TripModel) obj;
            return id.equals(trip.getId());

        }
        return false;
    }

    public enum State {
        CURRENT, UPCOMING, PAST
    }
}


