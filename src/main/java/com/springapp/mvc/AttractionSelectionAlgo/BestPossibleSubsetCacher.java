package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by kartik.k on 10/7/2014.
 */
public class BestPossibleSubsetCacher {
    String city;
    Double distanceMatrix[][];
    Double gratificationScoreArray [];
    private ArrayList<Attraction> sortedListOfAttractions;
    GratificationScoreCalculator scoreCalculator;

    public TreeMap<Double, Trip> tripsSortedByTimeTakenAsc(){

        TreeMap<Double,Trip> tripsStepCorners = new TreeMap<Double, Trip>();
        int stepsizeForDisplay = 1000000;
        for(Integer subsetOfAttractionIndex =15*(int)Math.pow(2, sortedListOfAttractions.size()-4);subsetOfAttractionIndex<Math.pow(2, sortedListOfAttractions.size());subsetOfAttractionIndex+=4){
            if(subsetOfAttractionIndex% stepsizeForDisplay == 0){
                System.out.println(Integer.toString(subsetOfAttractionIndex/ stepsizeForDisplay)+" steps done..(mill)");
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
                System.out.println("found a good trip :" + Integer.toBinaryString(currentTrip.getAttractionsVisitedBitArray()));
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
        Collections.sort(sortedListOfAttractions,new Comparator<Attraction>() {
            @Override
            public int compare(Attraction o1, Attraction o2) {
                return (int) (scoreCalculator.getGratificationScoreForAttraction(o1) - scoreCalculator.getGratificationScoreForAttraction(o2));
            }
        });
        distanceMatrix= new Double[sortedListOfAttractions.size()][sortedListOfAttractions.size()];
        gratificationScoreArray =new Double[sortedListOfAttractions.size()];
        for (int row = 0;row< sortedListOfAttractions.size();row++){
            for (int col = 0;col< sortedListOfAttractions.size();col++){
                if(row==col) {
                    distanceMatrix[row][col] = Double.valueOf(0);
                }
                else {
                    try {
                        distanceMatrix[row][col] = SqlQueryExecutor.getDistanceBetweenAttractions(
                                sortedListOfAttractions.get(row).getAttractionID(), sortedListOfAttractions.get(col).getAttractionID()
                        );
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for (int attractionNo = 0;attractionNo< sortedListOfAttractions.size();attractionNo++){
            gratificationScoreArray[attractionNo] = scoreCalculator.getGratificationScoreForAttraction(sortedListOfAttractions.get(attractionNo));
        }
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
        totalTimeSpent+=(estimatedMinDistanceTravelled(attractionsToVisit)/30000);
        return totalTimeSpent;
    }

    public double estimatedMinDistanceTravelled(Set<Attraction> attractionsToVisit) {
        double timeSpentInTravelling = 0;
        ArrayList<Attraction> listOfUnvisitedAttractions
                = new ArrayList<Attraction>(attractionsToVisit);
        Random rand = new Random();
        int randomAttractionIndex = rand.nextInt(listOfUnvisitedAttractions.size());
        Attraction lastVisitedAttraction = sortedListOfAttractions.get(randomAttractionIndex);
        listOfUnvisitedAttractions.remove(randomAttractionIndex);
        while (!listOfUnvisitedAttractions.isEmpty()) {
            double distanceFromClosestAttractionSeenSoFar = -1;
            Attraction closestAttraction = null;
            for (Attraction attraction:listOfUnvisitedAttractions){
                if(attraction.equals(lastVisitedAttraction)){
                    continue;
                }
                else {
                    if(distanceFromClosestAttractionSeenSoFar==-1){
                        distanceFromClosestAttractionSeenSoFar = getDistance(
                                sortedListOfAttractions.indexOf(lastVisitedAttraction),
                                sortedListOfAttractions.indexOf(attraction)
                        );
                        closestAttraction = attraction;
                    }
                    else {
                       double distanceFromSrc = distanceFromClosestAttractionSeenSoFar = getDistance(
                               sortedListOfAttractions.indexOf(lastVisitedAttraction),
                               sortedListOfAttractions.indexOf(attraction)
                       );
                        if(distanceFromClosestAttractionSeenSoFar>distanceFromSrc){
                            distanceFromClosestAttractionSeenSoFar = distanceFromSrc;
                            closestAttraction = attraction;
                        }
                    }
                }
            }
            if(closestAttraction!=null) {
                lastVisitedAttraction = closestAttraction;
//                System.out.print("'"+Double.toString(closestAttraction.getLatitude())+","+Double.toString(closestAttraction.getLongitude()));

                timeSpentInTravelling += distanceFromClosestAttractionSeenSoFar;
            }
            listOfUnvisitedAttractions.remove(lastVisitedAttraction);
        }
        return timeSpentInTravelling;
    }

    double getDistance(int srcAttrattionIndex, int destAttractionIndex) {
//        Double latDifference = latSrc-latDest;
//        Double longDifference = longSrc -longDest;
//        double a = (Math.sin(latDifference/2))*(Math.sin(latDifference/2)) +
//                Math.cos(latSrc) * Math.cos(latDest)
//                * (Math.sin(longDifference/2))*(Math.sin(longDifference/2));
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return distanceMatrix[srcAttrattionIndex][destAttractionIndex];
    }
}
