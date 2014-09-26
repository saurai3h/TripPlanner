package com.springapp.mvc.Models;

/**
 * Created by kartik.k on 9/26/2014.
 */
public class Attraction {
    private int noOfReviews;
    private float noOfStars;
    private String name;
    private String cityName;

    public int getNoOfReviews() {
        return noOfReviews;
    }

    public void setNoOfReviews(int noOfReviews) {
        this.noOfReviews = noOfReviews;
    }

    public float getNoOfStars() {
        return noOfStars;
    }

    public void setNoOfStars(float noOfStars) {
        this.noOfStars = noOfStars;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
