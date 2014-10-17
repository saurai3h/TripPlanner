package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;
import com.springapp.mvc.Utility.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kartik.k on 9/26/2014.
 */
public class AttractionSelectorSimple extends AttractionSelector {
    public AttractionSelectorSimple(GratificationScoreCalculatorSimple gratificationScoreCalculatorSimple) {
        super(new GratificationScoreCalculatorSimple());
    }

    @Override
    public ArrayList<java.util.List<Attraction>> selectAttraction(String cityName, int noOfDays, int mode) {
        ArrayList<Attraction> listOfAllAttractions = SqlQueryExecutor.getAllAttractionsForACity(cityName);
        DistanceCalculator distanceCalculator = SqlQueryExecutor.getDistanceMatrix(cityName);

        Collections.sort(listOfAllAttractions,new Comparator<Attraction>() {
            @Override
            public int compare(Attraction o1, Attraction o2) {
                return (int) (gratificationScoreCalculator.getGratificationScoreForAttraction(o2) - gratificationScoreCalculator.getGratificationScoreForAttraction(o1));
            }
        });
        ArrayList<List<Attraction>> listOfSchedulesForDays = new ArrayList<List<Attraction>>();
        int attractionsCoveredSoFar = 0;
        for(int dayNo = 0; dayNo<noOfDays; dayNo++){
            ArrayList<Attraction> scheduleForThisDay = new ArrayList<Attraction>();
            double lengthOfTheDay = 0.0;
            Attraction prevAttraction = null;
            int atttractionsSeenToday=0;
            boolean shouldContinue = true;
            while (shouldContinue){
                Attraction nextBestAttraction = listOfAllAttractions.get(attractionsCoveredSoFar + atttractionsSeenToday);
                scheduleForThisDay.add(nextBestAttraction);
                lengthOfTheDay += nextBestAttraction.getVisitTime();
                if (prevAttraction != null) {
                    lengthOfTheDay += distanceCalculator.getDistance(prevAttraction, nextBestAttraction);
                }
                atttractionsSeenToday++;
                prevAttraction = nextBestAttraction;
                if(mode==1) {
                    shouldContinue = lengthOfTheDay < Constants.getMIN_AVG_TRAVEL_TIME_PER_DAY(mode);
                }
                else if(mode == 2){
                    shouldContinue = lengthOfTheDay<Constants.getMAX_AVG_TRAVEL_TIME_PER_DAY(mode);
                }
                else {
                    System.out.println("check the value of mode. exiting..");
                    break;
                }

            }
            if(lengthOfTheDay>Constants.getMAX_DAY_LENGTH_ABSOLUTE(mode)){
                atttractionsSeenToday--;
                scheduleForThisDay.remove(atttractionsSeenToday);
            }
            attractionsCoveredSoFar=atttractionsSeenToday;
            Attraction westernmostAttraction = TSPSolverHeuristicsHelper.getExtremeAttraction(scheduleForThisDay);
            scheduleForThisDay = TSPSolverHeuristicsHelper.TSPSolverForAttractions(scheduleForThisDay,westernmostAttraction,distanceCalculator);
            scheduleForThisDay=TSPSolverHeuristicsHelper.apply2optHeuristicForTSP(distanceCalculator,scheduleForThisDay);
            listOfSchedulesForDays.add(scheduleForThisDay);
        }
        return listOfSchedulesForDays;
    }
}
