package com.henriquenfaria.wisetrip.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class ExpenseHeaderModel implements Serializable, Parcelable {

    public static final Parcelable.Creator<ExpenseHeaderModel> CREATOR = new Parcelable
            .Creator<ExpenseHeaderModel>() {
        @Override
        public ExpenseHeaderModel createFromParcel(Parcel source) {
            return new ExpenseHeaderModel(source);
        }

        @Override
        public ExpenseHeaderModel[] newArray(int size) {
            return new ExpenseHeaderModel[size];
        }
    };
    private Long id;
    private String title;

    public ExpenseHeaderModel() {
    }

    protected ExpenseHeaderModel(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.title = in.readString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpenseHeaderModel) {
            ExpenseHeaderModel inItem = (ExpenseHeaderModel) obj;
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
