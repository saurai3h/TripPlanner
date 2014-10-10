package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by kartik.k on 10/10/2014.
 */
public class AttractionSelectorForTripsTooSmall extends AttractionSelector {

    public AttractionSelectorForTripsTooSmall(GratificationScoreCalculator gratificationScoreCalculator) {
        super(gratificationScoreCalculator);
    }

    @Override
    public ArrayList<List<Attraction>> selectAttraction(String cityName, int noOfDays) {
        ArrayList<Attraction> listOfAllAttractions = SqlQueryExecutor.getAllAttractionsForACity(cityName);
        int totalNoOfAttractions = listOfAllAttractions.size();
        ArrayList<Attraction> tripPlan = new ArrayList<Attraction>();
        for (int firstAttractionNo = 0;firstAttractionNo<totalNoOfAttractions;firstAttractionNo++){
            tripPlan.add(listOfAllAttractions.get(firstAttractionNo));
            for (int maxNoOfAttractionsToBeSeen=1;maxNoOfAttractionsToBeSeen<5;maxNoOfAttractionsToBeSeen++){
                for(int subsequentAttractionIndex = 1;subsequentAttractionIndex<=maxNoOfAttractionsToBeSeen;subsequentAttractionIndex++){
                    
                }
            }
        }

        return null;
    }
}
