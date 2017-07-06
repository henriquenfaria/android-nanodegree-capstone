package com.henriquenfaria.wisetrip.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ExpenseModel implements Parcelable, Serializable {

    // TripModel id
    private String id;
    private String title;
    private String country;
    private Double amount;
    private Long date;

    public ExpenseModel() {
        // Required for Firebase
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.country);
        dest.writeValue(this.amount);
        dest.writeValue(this.date);
    }

    protected ExpenseModel(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.country = in.readString();
        this.amount = (Double) in.readValue(Double.class.getClassLoader());
        this.date = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Creator<ExpenseModel> CREATOR = new Creator<ExpenseModel>() {
        @Override
        public ExpenseModel createFromParcel(Parcel source) {
            return new ExpenseModel(source);
        }

        @Override
        public ExpenseModel[] newArray(int size) {
            return new ExpenseModel[size];
        }
    };


    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpenseModel) {
            ExpenseModel inItem = (ExpenseModel) obj;
            return id.equals(inItem.getId());
        }
        return false;
    }
}
