package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by kartik.k on 9/26/2014.
 */
public class AttractionSelectorSimple implements AttractionSelector {
    @Override
    public ArrayList<ArrayList<Attraction>> selectAttraction(final GratificationScoreCalculator gratificationScoreCalculator,
                                                             String cityName, int noOfDays) {
        int noOfAttractionsPerDay = 4;
        ArrayList<Attraction> listOfAllAttractions = SqlQueryExecutor.getAllAttractionsForACity(cityName);
        Collections.sort(listOfAllAttractions,new Comparator<Attraction>() {
            @Override
            public int compare(Attraction o1, Attraction o2) {
                return (int) (gratificationScoreCalculator.getGratificationScoreForAttraction(o1) - gratificationScoreCalculator.getGratificationScoreForAttraction(o2));
            }
        });
        ArrayList<ArrayList<Attraction>> listOfSchedulesForDays = new ArrayList<ArrayList<Attraction>>();
        for(int dayNo = 0; dayNo<noOfDays; dayNo++){
            ArrayList<Attraction> scheduleForThisDay = new ArrayList<Attraction>();
            scheduleForThisDay.addAll(listOfAllAttractions.subList(dayNo*noOfAttractionsPerDay,(dayNo+1)*noOfAttractionsPerDay-1));
            listOfSchedulesForDays.add(scheduleForThisDay);
        }
        return listOfSchedulesForDays;
    }
}
