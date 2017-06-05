package com.henriquenfaria.wisetrip.retrofit.models;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PlaceDetailsResult {

    @SerializedName("html_attributions")
    //TODO: Use @Expose?
    //@Expose
    private List<String> htmlAttributions = new ArrayList<>();

    @SerializedName("result")
    private Result result;

    @SerializedName("status")
    private String status;

    public List<String> getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List<String> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
