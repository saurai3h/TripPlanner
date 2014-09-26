package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;

/**
 * Created by kartik.k on 9/26/2014.
 */
public interface GratificationScoreCalculator {
    public double getGratificationScoreForAttraction(Attraction attraction);
}
