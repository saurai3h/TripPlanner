package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;

import java.util.ArrayList;

/**
 * Created by kartik.k on 9/26/2014.
 */
public abstract class AttractionSelector {
    public AttractionSelector(GratificationScoreCalculator gratificationScoreCalculator) {
        this.gratificationScoreCalculator = gratificationScoreCalculator;
    }

    public abstract ArrayList<ArrayList<Attraction>> selectAttraction(String cityName, int noOfDays);
    protected GratificationScoreCalculator gratificationScoreCalculator;
}
