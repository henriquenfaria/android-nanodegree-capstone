package com.henriquenfaria.wisetrip.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class BudgetModel implements Parcelable, Serializable {

    // TripModel id
    private String id;
    private String title;
    private String country;
    private String currency;
    private Double totalAmount;
    private Double remainingAmount;
    private Double notificationAt;

    public BudgetModel() {
        // Required for Firebase
    }

    protected BudgetModel(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.country = in.readString();
        this.currency = in.readString();
        this.totalAmount = (Double) in.readValue(Double.class.getClassLoader());
        this.remainingAmount = (Double) in.readValue(Double.class.getClassLoader());
        this.notificationAt = (Double) in.readValue(Double.class.getClassLoader());
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(Double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Double getNotificationAt() {
        return notificationAt;
    }

    public void setNotificationAt(Double notificationAt) {
        this.notificationAt = notificationAt;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BudgetModel) {
            BudgetModel inItem = (BudgetModel) obj;
            return id.equals(inItem.getId());
        }
        return false;
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
        dest.writeString(this.currency);
        dest.writeValue(this.totalAmount);
        dest.writeValue(this.remainingAmount);
        dest.writeValue(this.notificationAt);
    }

    public static final Creator<BudgetModel> CREATOR = new Creator<BudgetModel>() {
        @Override
        public BudgetModel createFromParcel(Parcel source) {
            return new BudgetModel(source);
        }

        @Override
        public BudgetModel[] newArray(int size) {
            return new BudgetModel[size];
        }
    };
}
