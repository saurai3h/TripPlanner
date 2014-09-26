package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;

import java.util.ArrayList;

/**
 * Created by kartik.k on 9/26/2014.
 */
public interface AttractionSelector {
    public ArrayList<ArrayList<Attraction>> selectAttraction(GratificationScoreCalculator gratificationScoreCalculator, String cityName, int noOfDays);
}
