package com.springapp.mvc.Models;

/**
 * Created by kartik.k on 9/26/2014.
 */
public class Attraction {
    private int noOfReviews;
    private float noOfStars;
    private String name;
    private String cityName;



    public Attraction(int noOfReviews, float noOfStars, String name, String cityName) {
        this.noOfReviews = noOfReviews;
        this.noOfStars = noOfStars;
        this.name = name;
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getName() {
        return name;
    }

    public int getNoOfReviews() {
        return noOfReviews;
    }

    public float getNoOfStars() {
        return noOfStars;
    }
}
