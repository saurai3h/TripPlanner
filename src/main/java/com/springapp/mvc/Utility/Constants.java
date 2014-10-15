package com.springapp.mvc.Utility;

/**
 * Created by Saurabh Paliwal on 25/9/14.
 */
public class Constants {
    public static final String SQL_DB_HOST = "localhost";// 172.16.152.143 Kartik's machine.

    public static final Double MAX_LOWER_MARGIN_FOR_DAY_LENGTH_ADVANCED_ATTRACTION_SELECTOR = 2.25;
    public static final Double MAX_HIGHER_MARGIN_FOR_DAY_LENGTH_ADVANCED_ATTRACTION_SELECTOR = 2.25;

    private static final double deltaValue = 1.5;
    private static final double MAX_AVG_TRAVEL_TIME_PER_DAY = 10.5;
    private static final double MIN_AVG_TRAVEL_TIME_PER_DAY = 7.5;
    private static final double MAX_DAY_LENGTH_ABSOLUTE = 12;
    private static final double MIN_DAY_LENGTH_ABSOLUTE = 5;

    public static final String[] LIST_OF_CITIES = {"Bangkok", "London", "Milan", "Paris", "Rome",
            "Singapore", "Shanghai", "New York", "Amsterdam", "Istanbul", "Tokyo",
            "Dubai", "Vienna", "Kuala Lumpur", "Taipei", "Hong Kong",
            "Barcelona", "Los Angeles","Mumbai"};

    public static final int getNoOfAttractions(String cityName){
        if(cityName.equals("Riyadh")){
            return 17;
        }
        else {
            return 30;
        }
    }

    public static double getMAX_AVG_TRAVEL_TIME_PER_DAY(int mode) {
        if(mode < 1 || mode > 2) mode = 2;
        return MAX_AVG_TRAVEL_TIME_PER_DAY + (mode-2)*deltaValue;
    }

    public static double getMIN_AVG_TRAVEL_TIME_PER_DAY(int mode) {
        if(mode < 1 || mode > 2) mode = 2;
        return MIN_AVG_TRAVEL_TIME_PER_DAY + (mode-2)*deltaValue;
    }

    public static double getMAX_DAY_LENGTH_ABSOLUTE(int mode) {
        if(mode < 1 || mode > 2) mode = 2;
        return MAX_DAY_LENGTH_ABSOLUTE + (mode-2)*deltaValue;
    }

    public static double getMIN_DAY_LENGTH_ABSOLUTE(int mode) {
        if(mode < 1 || mode > 2) mode = 2;
        return MIN_DAY_LENGTH_ABSOLUTE + (mode-2)*deltaValue;
    }
}
