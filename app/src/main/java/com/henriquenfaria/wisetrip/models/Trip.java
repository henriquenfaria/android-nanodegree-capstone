package com.henriquenfaria.wisetrip.models;


public class Trip {

    private String mTitle;
    private long mStartDate;
    private long mEndDate;
    private String[] mCountries;

    public Trip(String title, long startDate, long endDate, String[] countries) {
        mTitle = title;
        mStartDate = startDate;
        mEndDate = endDate;
        mCountries = countries;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public long getStartDate() {
        return mStartDate;
    }

    public void setStartDate(long startDate) {
        mStartDate = startDate;
    }

    public long getEndDate() {
        return mEndDate;
    }

    public void setEndDate(long endDate) {
        mEndDate = endDate;
    }

    public String[] getCountries() {
        return mCountries;
    }

    public void setCountries(String[] countries) {
        mCountries = countries;
    }
}


