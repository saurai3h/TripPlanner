package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;

import java.util.*;

/**
 * Created by kartik.k on 10/8/2014.
 */
public class AttractionSelectorBestSubsetOfAttractions extends AttractionSelector {

    public AttractionSelectorBestSubsetOfAttractions(GratificationScoreCalculator gratificationScoreCalculator) {
        super(gratificationScoreCalculator);
    }

    @Override
    public ArrayList<ArrayList<Attraction>> selectAttraction(String cityName, int noOfDays) {
        TreeMap<Double,Integer> tripsAtCornersOfStepFunction = SqlQueryExecutor.getDurationBitStringMapForCornerTrips(cityName);
        Double minTravelTimeInHrs = 5.5*noOfDays;
        Double maxTravelTimeInHrs = 12.5*noOfDays;
        ArrayList<Attraction> sortedListOfAttractions = SqlQueryExecutor.getAllAttractionsForACity(cityName);
        SortedMap<Double, Integer> feasibleTripMap = tripsAtCornersOfStepFunction.subMap(minTravelTimeInHrs, maxTravelTimeInHrs);
        Double shortestFeasibleTripKey = tripsAtCornersOfStepFunction.floorKey(minTravelTimeInHrs);
        if(shortestFeasibleTripKey == null){
            shortestFeasibleTripKey = tripsAtCornersOfStepFunction.firstKey();
            minTravelTimeInHrs = 0.0;
        }
        Double minFeasibleGratificationScore = getGratificationScoreFromBitString(tripsAtCornersOfStepFunction.get(shortestFeasibleTripKey),sortedListOfAttractions);

        Collections.sort(sortedListOfAttractions,new Comparator<Attraction>() {
            @Override
            public int compare(Attraction o1, Attraction o2) {
                return (int) (gratificationScoreCalculator.getGratificationScoreForAttraction(o1) - gratificationScoreCalculator.getGratificationScoreForAttraction(o2));
            }
        });
        double maxRewardRatio=0;
        Set<Attraction> mostRewardingAttractionSet = new HashSet<Attraction>();
        for(Double feasibleTripDuration:feasibleTripMap.keySet()){

            double extraTimeSpentOverMinTime = feasibleTripDuration - minTravelTimeInHrs;
            Integer bitString = feasibleTripMap.get(feasibleTripDuration);
            double additionalGratificationGot = getGratificationScoreFromBitString(bitString,sortedListOfAttractions) - minFeasibleGratificationScore;
            if(extraTimeSpentOverMinTime == 0){
                continue;
            }
            double rewardRatio = additionalGratificationGot/extraTimeSpentOverMinTime;

            if(rewardRatio>maxRewardRatio){
                maxRewardRatio = rewardRatio;
                mostRewardingAttractionSet = getAttractionSetFromBitString(bitString, sortedListOfAttractions);
            }
        }
        ArrayList<Attraction> listOfAttractionsToVisit = new ArrayList<Attraction>(mostRewardingAttractionSet);
        ArrayList<ArrayList<Attraction>> schedule = new ArrayList<ArrayList<Attraction>>();
        schedule.add(listOfAttractionsToVisit);
        return schedule;
    }



    public Set<Attraction> getAttractionSetFromBitString(Integer bitString,ArrayList<Attraction> sortedListOfAttractions) {

        Set<Attraction> subsetOfAttractions = new HashSet<Attraction>();

        for (int attractionNo=0;attractionNo< sortedListOfAttractions.size();attractionNo++){
            if(bitString%2==1){
                subsetOfAttractions.add(sortedListOfAttractions.get(attractionNo));
            }
            bitString = bitString >> 1;
        }
        return subsetOfAttractions;
    }

    public double getGratificationScoreFromBitString(Integer bitString, ArrayList<Attraction> sortedArrayListOfAttractions){
        Set<Attraction> setOfAttractions = getAttractionSetFromBitString(bitString, sortedArrayListOfAttractions);
        double totalGratificationScore = 0;
        for(Attraction attraction:setOfAttractions){
            totalGratificationScore+=gratificationScoreCalculator.getGratificationScoreForAttraction(attraction);
        }
        return totalGratificationScore;
    }

}
