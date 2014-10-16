package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;

import java.util.*;

/**
 * Created by kartik.k on 10/7/2014.
 */
public class BestPossibleSubsetCacher {


    String city;
    Double gratificationScoreArray [];
    private ArrayList<Attraction> sortedListOfAttractions;
    GratificationScoreCalculator scoreCalculator;
    private DistanceCalculator<Attraction> distanceCalculator;
    private long startTime;

    public TreeMap<Double, Trip> tripsSortedByTimeTakenAsc(){

        TreeMap<Double, Trip> tripsStepCorners = getBestSubsetsOfAttractionStepFunction();



        for(Double tripDuration:tripsStepCorners.keySet()){
            SqlQueryExecutor.storeTripStepFunctionCornerInCache(city,tripsStepCorners.get(tripDuration));
        }
        return tripsStepCorners;
    }

    private TreeMap<Double, Trip> getBestSubsetsOfAttractionStepFunction() {
        TreeMap<Double,Trip> tripsStepCorners = new TreeMap<Double, Trip>();
        int stepsizeForDisplay = 1000000;
        for(Integer subsetOfAttractionIndex =15*(int)Math.pow(2, sortedListOfAttractions.size()-4);subsetOfAttractionIndex<Math.pow(2, sortedListOfAttractions.size());subsetOfAttractionIndex+=4){
            if(subsetOfAttractionIndex% stepsizeForDisplay == 0){
                System.out.println(Integer.toString(subsetOfAttractionIndex / stepsizeForDisplay) + " steps done..(mill) at time "+Long.toString(new Date().getTime() - startTime));
            }
            Set<Attraction> subsetOfAttractions = getAttractionSetFromBitString(subsetOfAttractionIndex);
            double lengthOfCurrentTrip =getMinTimeRequiredToVisitGivenAttractions(subsetOfAttractions);
            Trip currentTrip =new Trip(subsetOfAttractionIndex,getGratificationScoreFromBitString(subsetOfAttractionIndex),lengthOfCurrentTrip);
            Map.Entry<Double, Trip> nearestSmallerTripEntry = tripsStepCorners.lowerEntry(lengthOfCurrentTrip);
            if(nearestSmallerTripEntry==null || nearestSmallerTripEntry.getValue().getGratificationScore()<currentTrip.getGratificationScore()){
                Map.Entry<Double, Trip> nearestLargerTripEntry = tripsStepCorners.higherEntry(lengthOfCurrentTrip);
                while (nearestLargerTripEntry!=null && nearestLargerTripEntry.getValue().getGratificationScore()<currentTrip.getGratificationScore()){
                    tripsStepCorners.remove(nearestLargerTripEntry.getKey());
                    nearestLargerTripEntry = tripsStepCorners.higherEntry(lengthOfCurrentTrip);
                }
                tripsStepCorners.put(lengthOfCurrentTrip,currentTrip);
//                System.out.println("found a good trip :" + Integer.toBinaryString(currentTrip.getAttractionsVisitedBitArray()));
            }

        }
        return tripsStepCorners;
    }

    public Set<Attraction> getAttractionSetFromBitString(Integer bitString) {
        Set<Attraction> subsetOfAttractions = new HashSet<Attraction>();
        for (int attractionNo=0;attractionNo< sortedListOfAttractions.size();attractionNo++){
            if(bitString%2==1){
                subsetOfAttractions.add(sortedListOfAttractions.get(attractionNo));
            }
            bitString = bitString >> 1;
        }
        return subsetOfAttractions;
    }

    public BestPossibleSubsetCacher(String city, final GratificationScoreCalculator scoreCalculator) {
        this.city = city;
        this.scoreCalculator = scoreCalculator;
        sortedListOfAttractions = SqlQueryExecutor.getAllAttractionsForACity(city);
        gratificationScoreArray =new Double[sortedListOfAttractions.size()];
        Collections.sort(sortedListOfAttractions,new Comparator<Attraction>() {
            @Override
            public int compare(Attraction o1, Attraction o2) {
                return (int) (scoreCalculator.getGratificationScoreForAttraction(o1) - scoreCalculator.getGratificationScoreForAttraction(o2));
            }
        });
        distanceCalculator = SqlQueryExecutor.getDistanceMatrix(city);


        for (int attractionNo = 0;attractionNo< sortedListOfAttractions.size();attractionNo++){
            gratificationScoreArray[attractionNo] = scoreCalculator.getGratificationScoreForAttraction(sortedListOfAttractions.get(attractionNo));
        }
        startTime = new Date().getTime();
    }

    public double getGratificationScoreFromBitString(long bitString){
        int bitNo = 0;
        double totalGratificationScore = 0;
        while (bitString!=0){
            if(bitString%2==1){
                totalGratificationScore+=gratificationScoreArray[bitNo];
            }
            bitString = bitString>>1;
            bitNo++;
        }
        return totalGratificationScore;
    }

    public double getMinTimeRequiredToVisitGivenAttractions(Set<Attraction> attractionsToVisit) {
        double totalTimeSpent = 0;
        int noOfAttractionsToVisit = attractionsToVisit.size();
        for (Attraction attraction : attractionsToVisit) {
            totalTimeSpent += attraction.getVisitTime();
        }
        totalTimeSpent += ((estimatedMinDistanceTravelled(attractionsToVisit) / 30000) * 0.666);
        return totalTimeSpent;
    }

    public double estimatedMinDistanceTravelled(Set<Attraction> attractionsToVisit) {
        Random rand = new Random();
        int randomAttractionIndex = rand.nextInt(attractionsToVisit.size());
        Attraction lastVisitedAttraction = sortedListOfAttractions.get(randomAttractionIndex);
        Attraction westernmostAttraction = null;
        for (Attraction attraction:attractionsToVisit){
            if(westernmostAttraction==null||westernmostAttraction.getLongitude()>attraction.getLongitude()){
                westernmostAttraction=attraction;
            }
        }

        ArrayList<Attraction> orderOfTraversalOfAttractions = TSPSolverHeuristicsHelper.TSPSolverForAttractions(attractionsToVisit, westernmostAttraction,
                distanceCalculator);
//        ArrayList<Attraction> orderAfter2opt = TSPSolverHeuristicsHelper.apply2optHeuristicForTSP(distanceCalculator,orderOfTraversalOfAttractions);
        Double timeSpentInTravelling = 0.0;
        Attraction previousAttraction = null;
        for(Attraction attraction:orderOfTraversalOfAttractions){
            if (previousAttraction != null) {
                timeSpentInTravelling+=distanceCalculator.getDistance(attraction,previousAttraction);
            }
            previousAttraction = attraction;

        }
        return timeSpentInTravelling;
    }


    public TreeMap<Double, Trip> getSmallBestSubsetsOfAttractionStepFunction() {
        TreeMap<Double,Trip> tripsStepCorners = new TreeMap<Double, Trip>();
        int stepsizeForDisplay = 1000000;
        for(Integer subsetOfAttractionIndex =1;subsetOfAttractionIndex<Math.pow(2, sortedListOfAttractions.size());subsetOfAttractionIndex+=4){
            if(subsetOfAttractionIndex% stepsizeForDisplay == 0){
                System.out.println(Integer.toString(subsetOfAttractionIndex / stepsizeForDisplay) + " steps done..(mill)");
            }
            if(countNoOfSetBits(subsetOfAttractionIndex)>4){
                continue;
            }
            Set<Attraction> subsetOfAttractions = getAttractionSetFromBitString(subsetOfAttractionIndex);
            double lengthOfCurrentTrip =getMinTimeRequiredToVisitGivenAttractions(subsetOfAttractions);
            Trip currentTrip =new Trip(subsetOfAttractionIndex,getGratificationScoreFromBitString(subsetOfAttractionIndex),lengthOfCurrentTrip);
            Map.Entry<Double, Trip> nearestSmallerTripEntry = tripsStepCorners.lowerEntry(lengthOfCurrentTrip);
            if(nearestSmallerTripEntry==null || nearestSmallerTripEntry.getValue().getGratificationScore()<currentTrip.getGratificationScore()){
                Map.Entry<Double, Trip> nearestLargerTripEntry = tripsStepCorners.higherEntry(lengthOfCurrentTrip);
                while (nearestLargerTripEntry!=null && nearestLargerTripEntry.getValue().getGratificationScore()<currentTrip.getGratificationScore()){
                    tripsStepCorners.remove(nearestLargerTripEntry.getKey());
                    nearestLargerTripEntry = tripsStepCorners.higherEntry(lengthOfCurrentTrip);
                }
                tripsStepCorners.put(lengthOfCurrentTrip,currentTrip);
//                System.out.println("found a good trip :" + Integer.toBinaryString(currentTrip.getAttractionsVisitedBitArray()));
            }

        }
        return tripsStepCorners;
    }

    int countNoOfSetBits(int n){
        int count = 0;
        while (n>0){
            n &= (n-1) ;
            count++;
        }
        return count;
    }
}
