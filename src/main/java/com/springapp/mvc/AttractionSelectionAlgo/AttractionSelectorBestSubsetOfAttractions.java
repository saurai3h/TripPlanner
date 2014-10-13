package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;
import com.springapp.mvc.Utility.Constants;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by kartik.k on 10/8/2014.
 */
public class AttractionSelectorBestSubsetOfAttractions extends AttractionSelector {

    public AttractionSelectorBestSubsetOfAttractions(GratificationScoreCalculator gratificationScoreCalculator) {
        super(gratificationScoreCalculator);
    }

    @Override
    public ArrayList<List<Attraction>> selectAttraction(String cityName, int noOfDays) {
        TreeMap<Double,Integer> tripsAtCornersOfStepFunction = SqlQueryExecutor.getDurationBitStringMapForCornerTrips(cityName);
        return getBestScheduleFromBitStringMap(cityName, noOfDays, tripsAtCornersOfStepFunction);
    }

    public ArrayList<List<Attraction>> getBestScheduleFromBitStringMap(String cityName, int noOfDays, TreeMap<Double, Integer> tripsAtCornersOfStepFunction) {
        Double minTravelTimeInHrs = Constants.MIN_AVG_TRAVEL_TIME_PER_DAY*noOfDays;
        Double maxTravelTimeInHrs = Constants.MAX_AVG_TRAVEL_TIME_PER_DAY *noOfDays;
        ArrayList<Attraction> sortedListOfAttractions = SqlQueryExecutor.getAllAttractionsForACity(cityName);
        SortedMap<Double, Integer> feasibleTripMap = tripsAtCornersOfStepFunction.subMap(minTravelTimeInHrs, maxTravelTimeInHrs);
        Double shortestFeasibleTripKey = tripsAtCornersOfStepFunction.floorKey(minTravelTimeInHrs);
        if(shortestFeasibleTripKey == null){
            shortestFeasibleTripKey = tripsAtCornersOfStepFunction.firstKey();
            minTravelTimeInHrs = 0.0;
            }
        Double minFeasibleGratificationScore = getGratificationScoreFromBitString(tripsAtCornersOfStepFunction.get(shortestFeasibleTripKey),sortedListOfAttractions);

        Collections.sort(sortedListOfAttractions, new Comparator<Attraction>() {
            @Override
            public int compare(Attraction o1, Attraction o2) {
                return (int) (gratificationScoreCalculator.getGratificationScoreForAttraction(o1) - gratificationScoreCalculator.getGratificationScoreForAttraction(o2));
            }
        });
        double maxRewardRatio=0;
        ArrayList<Attraction> mostRewardingAttractionSet = new ArrayList<Attraction>();
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
                mostRewardingAttractionSet = getAttractionListFromBitString(bitString, sortedListOfAttractions);
            }
        }
        ArrayList<List<Attraction>> basicSchedule = splitSetOfAttractionSetIntoDays(mostRewardingAttractionSet, noOfDays);
        return basicSchedule;
    }

    private ArrayList<List<Attraction>> splitSetOfAttractionSetIntoDays(final ArrayList<Attraction> listOfAttractions, int noOfDays) {
        ArrayList<List<Attraction>> schedule = new ArrayList<List<Attraction>>();


        int noOfAttractions = listOfAttractions.size();
        final Double[][] distanceMatrix = getDistanceMatrix(listOfAttractions);

        Attraction westernmostAttraction = null;
        for (Attraction attraction:listOfAttractions){
            if(westernmostAttraction==null||westernmostAttraction.getLongitude()>attraction.getLongitude()){
                westernmostAttraction=attraction;
            }
        }
        ArrayList<Attraction> orderOfTraversalAfterBasicTSPHeurisic = BestPossibleSubsetCacher.TSPSolverForAttractions(
                listOfAttractions, westernmostAttraction,
                new DistanceCalculator<Attraction>() {
                    @Override
                    public double getDistance(Attraction src, Attraction dest) {
                        return distanceMatrix[listOfAttractions.indexOf(src)][listOfAttractions.indexOf(dest)];
                    }
                });
        ArrayList<Integer> reorderedAttracionsIndexArray =new ArrayList<Integer>();
        for (int i=0;i<noOfAttractions;i++) {
            reorderedAttracionsIndexArray.add(listOfAttractions.indexOf(orderOfTraversalAfterBasicTSPHeurisic.get(i)));
        }

        reorderedAttracionsIndexArray = apply2optTechniqueForAttractionTSP(distanceMatrix, reorderedAttracionsIndexArray);

        orderOfTraversalAfterBasicTSPHeurisic = new ArrayList<Attraction>();
        for (Integer reorderedAttractionIndex=0;reorderedAttractionIndex<noOfAttractions;reorderedAttractionIndex++){
            orderOfTraversalAfterBasicTSPHeurisic.add(listOfAttractions.get(reorderedAttracionsIndexArray.get(reorderedAttractionIndex)));
        }

        Double totalTimeNeededToCompleteTripNonStop = 0.0;
        Attraction prevAttraction = null;
        for (Attraction attraction:orderOfTraversalAfterBasicTSPHeurisic){
            if(prevAttraction!=null){
                totalTimeNeededToCompleteTripNonStop += distanceMatrix[listOfAttractions.indexOf(prevAttraction)][listOfAttractions.indexOf(attraction)];
            }
            totalTimeNeededToCompleteTripNonStop+=attraction.getVisitTime();
        }
            Double avgTimeNeededToSpendEveryRemainingDay = totalTimeNeededToCompleteTripNonStop/noOfDays;


        int noOfDaysRemaining = noOfDays;
        int indexOfFirstAttractionForToday = 0;
        double timeSpentSoFarToday=0.0;
        for (int i = 0; i < orderOfTraversalAfterBasicTSPHeurisic.size(); i++) {
            Attraction attraction = orderOfTraversalAfterBasicTSPHeurisic.get(i);

            timeSpentSoFarToday += attraction.getVisitTime();

            if (timeSpentSoFarToday > avgTimeNeededToSpendEveryRemainingDay- Constants.MAX_LOWER_MARGIN_FOR_DAY_LENGTH_ADVANCED_ATTRACTION_SELECTOR
                    && timeSpentSoFarToday>Constants.MIN_DAY_LENGTH_ABSOLUTE){
                //need to look ahead and find the best point to segment this day
                Attraction curAttraction = orderOfTraversalAfterBasicTSPHeurisic.get(i);
                Attraction nextAttraction = orderOfTraversalAfterBasicTSPHeurisic.get(i + 1);
                double largestDistanceSeenSoFar = 0.0;
                int lastAttractionForBestSegmentSeenSoFar = i;
                double timeSpentAfterBestSegmentSoFar = timeSpentSoFarToday;
                while (timeSpentSoFarToday<avgTimeNeededToSpendEveryRemainingDay+Constants.MAX_HIGHER_MARGIN_FOR_DAY_LENGTH_ADVANCED_ATTRACTION_SELECTOR
                        &&timeSpentAfterBestSegmentSoFar<Constants.MAX_DAY_LENGTH_ABSOLUTE){

                    if (distanceMatrix[listOfAttractions.indexOf(curAttraction)][listOfAttractions.indexOf(nextAttraction)]>largestDistanceSeenSoFar){
                        largestDistanceSeenSoFar = distanceMatrix[listOfAttractions.indexOf(curAttraction)][listOfAttractions.indexOf(nextAttraction)];
                        lastAttractionForBestSegmentSeenSoFar = i;
                        timeSpentAfterBestSegmentSoFar = timeSpentSoFarToday;
                    }
                    i++;
                    timeSpentSoFarToday += distanceMatrix[listOfAttractions.indexOf(curAttraction)][listOfAttractions.indexOf(nextAttraction)];
                    curAttraction = nextAttraction;
                    nextAttraction = orderOfTraversalAfterBasicTSPHeurisic.get(i + 1);
                    timeSpentSoFarToday += curAttraction.getVisitTime();
                }
                i=lastAttractionForBestSegmentSeenSoFar;
                timeSpentSoFarToday = 0;
                avgTimeNeededToSpendEveryRemainingDay = (avgTimeNeededToSpendEveryRemainingDay*noOfDaysRemaining -
                timeSpentAfterBestSegmentSoFar-largestDistanceSeenSoFar)/(noOfDaysRemaining-1);
                noOfDaysRemaining--;
                schedule.add(orderOfTraversalAfterBasicTSPHeurisic.subList(indexOfFirstAttractionForToday,i+1));
                indexOfFirstAttractionForToday = i+1;
                if(noOfDaysRemaining==1){
                    schedule.add(orderOfTraversalAfterBasicTSPHeurisic.subList(i+1,orderOfTraversalAfterBasicTSPHeurisic.size()));
                    break;
                }
            }
            else {
                if (i < listOfAttractions.size()) {
                    timeSpentSoFarToday += distanceMatrix[listOfAttractions.indexOf(orderOfTraversalAfterBasicTSPHeurisic.get(i+1))]
                            [listOfAttractions.indexOf(attraction)];
                }
                else {
                    break;
                }
            }
        }

        if(schedule.size()>1) {
            Collections.sort(schedule, new Comparator<List<Attraction>>() {
                @Override
                public int compare(List<Attraction> o1, List<Attraction> o2) {
                    double firstDayLength = 0;
                    double secondDayLength = 0;
                    Attraction prevAttraction = null;
                    for (Attraction attraction : o1) {
                        if (prevAttraction != null) {
                            firstDayLength += distanceMatrix[listOfAttractions.indexOf(attraction)][listOfAttractions.indexOf(prevAttraction)];
                        }
                        firstDayLength += attraction.getVisitTime();
                    }
                    prevAttraction = null;
                    for (Attraction attraction : o2) {
                        if (prevAttraction != null) {
                            secondDayLength += distanceMatrix[listOfAttractions.indexOf(attraction)][listOfAttractions.indexOf(prevAttraction)];
                        }
                        secondDayLength += attraction.getVisitTime();
                    }
                    return (int) (firstDayLength - secondDayLength);
                }
            });
            List<Attraction> smallestDay = schedule.get(0);
            List<Attraction> secondSmallestDay = schedule.get(1);
//            Collections.shuffle(schedule);
            schedule.remove(smallestDay);
            schedule.remove(secondSmallestDay);
            schedule.add(smallestDay);
            schedule.add(0,secondSmallestDay);
        }
        return schedule;
    }

    private Double[][] getDistanceMatrix(ArrayList<Attraction> listOfAttractions) {
        int noOfAttractions = listOfAttractions.size();
        Double[][] distanceMatrix = new Double[noOfAttractions][noOfAttractions];
        for (int row = 0;row< noOfAttractions;row++){
            for (int col = 0;col< noOfAttractions;col++){
                if(row==col) {
                    distanceMatrix[row][col] = Double.valueOf(0);
                }
                else {
                    try {
                        distanceMatrix[row][col] = SqlQueryExecutor.getDistanceBetweenAttractions(
                                listOfAttractions.get(row).getAttractionID(), listOfAttractions.get(col).getAttractionID()
                        )/30000;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return distanceMatrix;
    }

    public ArrayList<Integer> apply2optTechniqueForAttractionTSP(Double[][] distanceMatrix, ArrayList<Integer> reorderedAttracionsIndexArray) {
        int noOfAttractions;
        int firstEdgeDest=1;
        noOfAttractions = reorderedAttracionsIndexArray.size();
        reorderedAttracionsIndexArray.add(reorderedAttracionsIndexArray.get(0));
        while (firstEdgeDest<noOfAttractions-2){
            int secondEdgeSrc = firstEdgeDest + 1;
            while (secondEdgeSrc<noOfAttractions-1){

                double currentEdgePairTotalLength = distanceMatrix[reorderedAttracionsIndexArray.get(firstEdgeDest - 1)][reorderedAttracionsIndexArray.get(firstEdgeDest)]+
                        distanceMatrix[reorderedAttracionsIndexArray.get(secondEdgeSrc)][reorderedAttracionsIndexArray.get(secondEdgeSrc + 1)];
                double totalEdgePairLengthAfterSwapping =distanceMatrix[reorderedAttracionsIndexArray.get(firstEdgeDest - 1)][reorderedAttracionsIndexArray.get(secondEdgeSrc)]+
                        distanceMatrix[reorderedAttracionsIndexArray.get(firstEdgeDest)][reorderedAttracionsIndexArray.get(secondEdgeSrc + 1)];
                if(totalEdgePairLengthAfterSwapping<currentEdgePairTotalLength){
                    //swap
                    ArrayList<Integer> newReorderedAttractionIndexArray = new ArrayList<Integer>();
                    newReorderedAttractionIndexArray.addAll(reorderedAttracionsIndexArray.subList(0,firstEdgeDest));
                    List<Integer> subListBetweenTheTwoEdges = reorderedAttracionsIndexArray.subList(firstEdgeDest, secondEdgeSrc + 1);
                    Collections.reverse(subListBetweenTheTwoEdges);
                    newReorderedAttractionIndexArray.addAll(subListBetweenTheTwoEdges);
                    newReorderedAttractionIndexArray.addAll(reorderedAttracionsIndexArray.subList(secondEdgeSrc+1,noOfAttractions));
                    reorderedAttracionsIndexArray = newReorderedAttractionIndexArray;
                    secondEdgeSrc = firstEdgeDest+1;
                }
                else {
                    secondEdgeSrc++;
                }
            }
            firstEdgeDest++;
        }
        return reorderedAttracionsIndexArray;
    }


    public ArrayList<Attraction> getAttractionListFromBitString(Integer bitString, ArrayList<Attraction> sortedListOfAttractions) {

        ArrayList<Attraction> subsetOfAttractions = new ArrayList<Attraction>();

        for (int attractionNo=0;attractionNo< sortedListOfAttractions.size();attractionNo++){
            if(bitString%2==1){
                subsetOfAttractions.add(0,sortedListOfAttractions.get(attractionNo));
            }
            bitString = bitString >> 1;
        }
        return subsetOfAttractions;
    }

    public double getGratificationScoreFromBitString(Integer bitString, ArrayList<Attraction> sortedArrayListOfAttractions){
        ArrayList<Attraction> setOfAttractions = getAttractionListFromBitString(bitString, sortedArrayListOfAttractions);
        double totalGratificationScore = 0;
        for(Attraction attraction:setOfAttractions){
            totalGratificationScore+=gratificationScoreCalculator.getGratificationScoreForAttraction(attraction);
        }
        return totalGratificationScore;
    }

    private double getTotalTimeSpentInTransitForASchedule(ArrayList<List<Attraction>> schedule, DistanceCalculator<Attraction> distanceCalculator){
        double totalTimeSpent = 0.0;
        for (List<Attraction> scheduleForOneDay:schedule){
            Attraction prevAttraction = null;
            for (Attraction curAttraction :scheduleForOneDay){
                if (prevAttraction!=null){
                    totalTimeSpent+= distanceCalculator.getDistance(prevAttraction,curAttraction);
                    prevAttraction = curAttraction;
                }
            }
        }
        return totalTimeSpent;
    }



}
