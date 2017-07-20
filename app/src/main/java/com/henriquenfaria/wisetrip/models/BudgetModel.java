package com.henriquenfaria.wisetrip.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BudgetModel implements Parcelable, Serializable {

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

    // TripModel id
    private String id;
    private String title;
    private String country;
    private String currency;
    private Double budgetAmount;
    private Double expensesAmount;
    private Double notificationAt;
    private Map<String, Double> expenses;

    public BudgetModel() {
        id = "";
        title = "";
        country = "";
        currency = "";
        budgetAmount = 0d;
        expensesAmount = 0d;
        notificationAt = 0d;
        expenses = new HashMap<>();
    }

    protected BudgetModel(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.country = in.readString();
        this.currency = in.readString();
        this.budgetAmount = (Double) in.readValue(Double.class.getClassLoader());
        this.expensesAmount = (Double) in.readValue(Double.class.getClassLoader());
        this.notificationAt = (Double) in.readValue(Double.class.getClassLoader());
        int expensesSize = in.readInt();
        this.expenses = new HashMap<>(expensesSize);
        for (int i = 0; i < expensesSize; i++) {
            String key = in.readString();
            Double value = (Double) in.readValue(Double.class.getClassLoader());
            this.expenses.put(key, value);
        }
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

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public Double getExpensesAmount() {
        return expensesAmount;
    }

    public void setExpensesAmount(Double expensesAmount) {
        this.expensesAmount = expensesAmount;
    }

    public Double getNotificationAt() {
        return notificationAt;
    }

    public void setNotificationAt(Double notificationAt) {
        this.notificationAt = notificationAt;
    }

    public Map<String, Double> getExpenses() {
        return expenses;
    }

    public void setExpenses(Map<String, Double> expenses) {
        this.expenses = expenses;
    }

    public void updateExpensesAmount() {
        Double updatedExpensesAmount = 0d;
        for (Double amount : expenses.values()) {
            updatedExpensesAmount += amount;
        }
        this.expensesAmount = updatedExpensesAmount;
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
        dest.writeValue(this.budgetAmount);
        dest.writeValue(this.expensesAmount);
        dest.writeValue(this.notificationAt);
        dest.writeInt(this.expenses.size());
        for (Map.Entry<String, Double> entry : this.expenses.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeValue(entry.getValue());
        }
    }
}
