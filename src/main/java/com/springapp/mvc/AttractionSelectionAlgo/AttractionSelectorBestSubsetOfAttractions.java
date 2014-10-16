package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;
import com.springapp.mvc.Utility.Constants;

import java.util.*;

/**
 * Created by kartik.k on 10/8/2014.
 */
public class AttractionSelectorBestSubsetOfAttractions extends AttractionSelector {

    private long startTime=0;

    public AttractionSelectorBestSubsetOfAttractions(GratificationScoreCalculator gratificationScoreCalculator) {
        super(gratificationScoreCalculator);
    }

    @Override
    public ArrayList<List<Attraction>> selectAttraction(String cityName, int noOfDays, int mode) {
        Double minTravelTimeInHrs = Constants.getMIN_AVG_TRAVEL_TIME_PER_DAY(mode) * noOfDays;
        Double maxTravelTimeInHrs = Constants.getMAX_AVG_TRAVEL_TIME_PER_DAY(mode) * noOfDays;
        log("initialization");
        TreeMap<Double,Integer> tripsAtCornersOfStepFunction = SqlQueryExecutor.getDurationBitStringMapForCornerTrips(cityName, minTravelTimeInHrs, maxTravelTimeInHrs);
        log("fetching cornerCache");
        return getBestScheduleFromBitStringMap(cityName, noOfDays, mode, tripsAtCornersOfStepFunction);
    }

       public ArrayList<List<Attraction>> getBestScheduleFromBitStringMap(String cityName, int noOfDays, int mode, TreeMap<Double, Integer> tripsAtCornersOfStepFunction) {
           Double minTravelTimeInHrs = Constants.getMIN_AVG_TRAVEL_TIME_PER_DAY(mode) * noOfDays;
           Double maxTravelTimeInHrs = Constants.getMAX_AVG_TRAVEL_TIME_PER_DAY(mode) * noOfDays;
           log("preparing to get all attractions for a city");
           ArrayList<Attraction> sortedListOfAttractions = SqlQueryExecutor.getAllAttractionsForACity(cityName);
           log("getting all attractions for a city");
           SortedMap<Double, Integer> feasibleTripMap = tripsAtCornersOfStepFunction.subMap(minTravelTimeInHrs, maxTravelTimeInHrs);
           Double shortestFeasibleTripKey = tripsAtCornersOfStepFunction.floorKey(minTravelTimeInHrs);
           if (shortestFeasibleTripKey == null) {
               shortestFeasibleTripKey = tripsAtCornersOfStepFunction.firstKey();
               minTravelTimeInHrs = shortestFeasibleTripKey;
           }
           Double minFeasibleGratificationScore = getGratificationScoreFromBitString(tripsAtCornersOfStepFunction.get(shortestFeasibleTripKey), sortedListOfAttractions);
           log("preparing to sort the list of attractions");
           Collections.sort(sortedListOfAttractions, new Comparator<Attraction>() {
               @Override
               public int compare(Attraction o1, Attraction o2) {
                   return (int) (gratificationScoreCalculator.getGratificationScoreForAttraction(o1) - gratificationScoreCalculator.getGratificationScoreForAttraction(o2));
               }
           });
           log("sorting the list of attractions");
           double maxRewardRatio = -1;
           ArrayList<Attraction> mostRewardingAttractionSet = new ArrayList<Attraction>();
           log("preparing to find the most rewarding subset of attractions");
           for (Double feasibleTripDuration : feasibleTripMap.keySet()) {

               double extraTimeSpentOverMinTime = feasibleTripDuration - minTravelTimeInHrs;
               Integer bitString = feasibleTripMap.get(feasibleTripDuration);
               double additionalGratificationGot = getGratificationScoreFromBitString(bitString, sortedListOfAttractions) - minFeasibleGratificationScore;

               double rewardRatio;
               if(extraTimeSpentOverMinTime!=0) {
                   rewardRatio = additionalGratificationGot / extraTimeSpentOverMinTime;
               }
               else {
                    rewardRatio=0;
               }
               double idealAvgTripDuration = (maxTravelTimeInHrs + minTravelTimeInHrs) / 2;
               double normalizedTripDurationDeviation = (feasibleTripDuration - idealAvgTripDuration) / (maxTravelTimeInHrs - idealAvgTripDuration);
               rewardRatio = rewardRatio * (1 - normalizedTripDurationDeviation * normalizedTripDurationDeviation);
               if (rewardRatio > maxRewardRatio) {
                   maxRewardRatio = rewardRatio;
                   mostRewardingAttractionSet = getAttractionListFromBitString(bitString, sortedListOfAttractions);
               }
           }
           log("finding the most rewarding subset of attractions");
           ArrayList<List<Attraction>> basicSchedule = splitSetOfAttractionSetIntoDays(mostRewardingAttractionSet, noOfDays, cityName, mode);
           log("everything");
           return basicSchedule;
       }

    private ArrayList<List<Attraction>> splitSetOfAttractionSetIntoDays(final ArrayList<Attraction> listOfAttractions, int noOfDays, final String cityName,int mode) {
    ArrayList<List<Attraction>> schedule = new ArrayList<List<Attraction>>();

        log("preparing for distance matrix calculation");
        int noOfAttractions = listOfAttractions.size();
        final DistanceCalculator<Attraction> distanceCalculator =  SqlQueryExecutor.getDistanceMatrix(cityName);
        log("distance matrix calculation" );

        Set<Attraction> extremeAttractions = TSPSolverHeuristicsHelper.getExtremeAttraction(listOfAttractions);
        Double minTimeInTransit = -1.0;
        log("preparing tsp solving");
        for(Attraction extremeAttraction:extremeAttractions) {
            ArrayList<Attraction> orderOfTraversalAfterBasicTSPHeurisic = TSPSolverHeuristicsHelper.TSPSolverForAttractions(
                    listOfAttractions, extremeAttraction, distanceCalculator);
            log("basic tsp");
            ArrayList<Attraction> orderAfter2opt = TSPSolverHeuristicsHelper.apply2optHeuristicForTSP(distanceCalculator, orderOfTraversalAfterBasicTSPHeurisic);
            log("2-opt");
            ArrayList<List<Attraction>> scheduleForThisExtreme = segmentTourOfAttractionsIntoDays(noOfDays, distanceCalculator, orderAfter2opt, mode);
            Double timeInTransitForThisSchedule = TSPSolverHeuristicsHelper.getTotalTimeSpentInTransitForASchedule(scheduleForThisExtreme, distanceCalculator);
            if (minTimeInTransit == -1.0 || minTimeInTransit > timeInTransitForThisSchedule) {
                minTimeInTransit = timeInTransitForThisSchedule;
                schedule = scheduleForThisExtreme;
            }
        }
        schedule = minimizeLoadOnFirstAndLastDay(schedule,distanceCalculator);
        return schedule;
    }

    private void log(String actionTaken) {
        if(startTime==0){
            startTime=new Date().getTime();
        }
        long timeTaken = new Date().getTime()-startTime;
        LOGGER.info("time is now "+Long.toString(timeTaken)+ ". finished "+actionTaken);
    }

    private ArrayList<List<Attraction>> segmentTourOfAttractionsIntoDays(int noOfDays, DistanceCalculator<Attraction> distanceCalculator, ArrayList<Attraction> tourOfAttractions,int mode) {
        Double totalTimeNeededToCompleteTripNonStop = TSPSolverHeuristicsHelper.getTotalTimeSpentOnADay(distanceCalculator, tourOfAttractions);
        Double avgTimeNeededToSpendEveryRemainingDay = totalTimeNeededToCompleteTripNonStop/noOfDays;

        ArrayList<List<Attraction>> schedule = new ArrayList<List<Attraction>>();
        int noOfDaysRemaining = noOfDays;
        int indexOfFirstAttractionForToday = 0;
        double timeSpentSoFarToday=0.0;
        for (int i = 0; i < tourOfAttractions.size(); i++) {
            Attraction attraction = tourOfAttractions.get(i);

            timeSpentSoFarToday += attraction.getVisitTime();

            if (timeSpentSoFarToday > avgTimeNeededToSpendEveryRemainingDay- Constants.MAX_DEVIATION_FROM_AVG_DAY_LENGTH
                    && timeSpentSoFarToday>Constants.getMIN_DAY_LENGTH_ABSOLUTE(mode)){
                Attraction curAttraction = tourOfAttractions.get(i);
                Attraction nextAttraction = tourOfAttractions.get(i + 1);
                double bestRewardForSegmentingSeenSoFar = 0.0;
                double distanceAfterLastAttractionOfBestSegmentSeenSoFar=0;
                int lastAttractionForBestSegmentSeenSoFar = i;
                double timeSpentAfterBestSegmentSoFar = timeSpentSoFarToday;
                while (timeSpentSoFarToday<avgTimeNeededToSpendEveryRemainingDay+Constants.MAX_DEVIATION_FROM_AVG_DAY_LENGTH
                        &&timeSpentAfterBestSegmentSoFar<Constants.getMAX_DAY_LENGTH_ABSOLUTE(mode)){

                    Double distBetweenCurAndNextAttraction = distanceCalculator.getDistance(curAttraction,nextAttraction);
                    double extraTimeSpentToday = timeSpentSoFarToday - totalTimeNeededToCompleteTripNonStop/noOfDays;
                    Double rewardForSegmentingAfterCurAttraction = distBetweenCurAndNextAttraction *
                            Math.pow(extraTimeSpentToday, 2);
                    if (rewardForSegmentingAfterCurAttraction >bestRewardForSegmentingSeenSoFar){
                        bestRewardForSegmentingSeenSoFar = rewardForSegmentingAfterCurAttraction;
                        distanceAfterLastAttractionOfBestSegmentSeenSoFar = distBetweenCurAndNextAttraction;
                        lastAttractionForBestSegmentSeenSoFar = i;
                        timeSpentAfterBestSegmentSoFar = timeSpentSoFarToday;
                    }
                    i++;
                    timeSpentSoFarToday += distBetweenCurAndNextAttraction;
                    curAttraction = nextAttraction;
                    if(i+1<tourOfAttractions.size()) {
                        nextAttraction = tourOfAttractions.get(i + 1);
                    }
                    else {
                        nextAttraction = null;
                    }
                    timeSpentSoFarToday += curAttraction.getVisitTime();
                }
                i=lastAttractionForBestSegmentSeenSoFar;
                timeSpentSoFarToday = 0;
                avgTimeNeededToSpendEveryRemainingDay = (avgTimeNeededToSpendEveryRemainingDay*noOfDaysRemaining -
                timeSpentAfterBestSegmentSoFar-distanceAfterLastAttractionOfBestSegmentSeenSoFar)/(noOfDaysRemaining-1);
                noOfDaysRemaining--;
                schedule.add(tourOfAttractions.subList(indexOfFirstAttractionForToday,i+1));
                indexOfFirstAttractionForToday = i+1;
                if(noOfDaysRemaining==1){
                    schedule.add(tourOfAttractions.subList(
                            i+1, tourOfAttractions.size()));
                    break;
                }
            }
            else {
                if (i < tourOfAttractions.size()+1) {
                    timeSpentSoFarToday += distanceCalculator.getDistance(
                            tourOfAttractions.get(i+1),attraction);
                }
                else {
                    break;
                }
            }
        }
        return schedule;
    }

    private ArrayList<List<Attraction>> minimizeLoadOnFirstAndLastDay(ArrayList<List<Attraction>> schedule, final DistanceCalculator<Attraction> distanceCalculator) {
        if(schedule.size()>1) {
            Comparator<List<Attraction>> dayComapratorBasedOnTotalTimeSpent = new Comparator<List<Attraction>>() {
                @Override
                public int compare(List<Attraction> o1, List<Attraction> o2) {
                    double additionalTimeSpentOnDay1wrtDay2 = TSPSolverHeuristicsHelper.getTotalTimeSpentOnADay(distanceCalculator, o1) -
                            TSPSolverHeuristicsHelper.getTotalTimeSpentOnADay(distanceCalculator, o2);
                    if (additionalTimeSpentOnDay1wrtDay2 > 0) {
                        return 1;
                    } else if (additionalTimeSpentOnDay1wrtDay2 < 0) {
                        return -1;
                    } else {
                        System.out.println("the two days " + o1.toString() + " and " + o2.toString() + " have exactly same timme spent!");
                        return 0;
                    }
                }
            };
            List<Attraction> smallestDay = Collections.min(schedule, dayComapratorBasedOnTotalTimeSpent);
            schedule.remove(smallestDay);
            List<Attraction> secondSmallestDay = Collections.min(schedule, dayComapratorBasedOnTotalTimeSpent);
            schedule.remove(secondSmallestDay);
//            Collections.shuffle(schedule);
            schedule.add(smallestDay);
            schedule.add(0,secondSmallestDay);
        }
        log("minimization of first/last day");
        return schedule;
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


}
