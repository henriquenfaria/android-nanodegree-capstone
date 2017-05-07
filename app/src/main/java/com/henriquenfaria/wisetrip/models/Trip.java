package com.henriquenfaria.wisetrip.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Trip {

    private String title;
    private long startDate;
    private long endDate;
    private List<String> countries;

    public Trip(String title, long startDate, long endDate, List<String> countries) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.countries = countries;
    }

    public Trip() {
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("countries", countries);
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

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }
}


