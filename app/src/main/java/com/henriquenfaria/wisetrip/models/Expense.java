package com.henriquenfaria.wisetrip.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Expense implements Parcelable, Comparable<Expense> {

    // Trip id
    private String id;
    private String title;
    private String country;
    private Double amount;
    private Long date;

    public Expense() {
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

    protected Expense(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.country = in.readString();
        this.amount = (Double) in.readValue(Double.class.getClassLoader());
        this.date = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Creator<Expense> CREATOR = new Creator<Expense>() {
        @Override
        public Expense createFromParcel(Parcel source) {
            return new Expense(source);
        }

        @Override
        public Expense[] newArray(int size) {
            return new Expense[size];
        }
    };

    @Override
    public int compareTo(@NonNull Expense expense) {
         return this.getDate().compareTo(expense.getDate());
    }

    @Override
    public int hashCode() {
         /* We only need to use id to compare if a Trip is the same. Since id is unique,
          we can ignore the other properties. It will increase the performance*/
        return new HashCodeBuilder(17, 31)
                .append(id)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Expense)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

         /* We only need to use id to compare if a Expense is the same. Since id is unique,
          we can ignore the other properties. It will increase the performance*/
        Expense expense = (Expense) obj;
        return new EqualsBuilder()
                .append(id, expense.id)
                .isEquals();
    }
}
