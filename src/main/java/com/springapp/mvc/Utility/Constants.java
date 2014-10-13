package com.springapp.mvc.Utility;

/**
 * Created by Saurabh Paliwal on 25/9/14.
 */
public class Constants {
    public static final String SQL_DB_HOST = "172.16.152.143";
    public static final Double MAX_LOWER_MARGIN_FOR_DAY_LENGTH_ADVANCED_ATTRACTION_SELECTOR = 2.25;
    public static final Double MAX_HIGHER_MARGIN_FOR_DAY_LENGTH_ADVANCED_ATTRACTION_SELECTOR = 2.25;
    public static final  double MAX_AVG_TRAVEL_TIME_PER_DAY = 10.5;
    public static final  double MIN_AVG_TRAVEL_TIME_PER_DAY = 7.5;
    public static final double MAX_DAY_LENGTH_ABSOLUTE = 12;
    public static final double MIN_DAY_LENGTH_ABSOLUTE = 5;
    public Double MIN_TIME_SPENT_ON_A_DAY = 7.0;
    public Double MAX_TIME_SPENT_ON_A_DAY = 12.5;
    public static final String[] LIST_OF_CITIES = {"Bangkok", "Seoul", "London", "Milan", "Paris", "Rome",
            "Singapore", "Shanghai", "New York", "Amsterdam", "Istanbul", "Tokyo",
            "Dubai", "Vienna", "Kuala Lumpur", "Taipei", "Hong Kong", "Riyadh",
            "Barcelona", "Los Angeles"};
    public static final int getNoOfAttractions(String cityName){
        if(cityName.equals("Riyadh")){
            return 17;
        }
        else {
            return 30;
        }
    }
}
