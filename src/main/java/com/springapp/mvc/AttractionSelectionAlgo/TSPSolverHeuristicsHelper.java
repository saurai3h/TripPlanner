package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;

import java.util.*;

/**
 * Created by kartik.k on 10/15/2014.
 */
public abstract   class TSPSolverHeuristicsHelper {
    public static ArrayList<Attraction> apply2optHeuristicForTSP(DistanceCalculator<Attraction> distanceCalculator, ArrayList<Attraction> orderOfTraversalAfterBasicTSPHeurisic) {
        int noOfAttractions;
        int firstEdgeDest=1;
        boolean bool=false;
        long startTime = 0;
        noOfAttractions = orderOfTraversalAfterBasicTSPHeurisic.size();
        orderOfTraversalAfterBasicTSPHeurisic.add(orderOfTraversalAfterBasicTSPHeurisic.get(0));
        while (firstEdgeDest< noOfAttractions -2){
            int secondEdgeSrc = firstEdgeDest + 1;
            while (secondEdgeSrc< noOfAttractions -1){
                double currentEdgePairTotalLength =
                        distanceCalculator.getDistance(orderOfTraversalAfterBasicTSPHeurisic.get(firstEdgeDest-1),
                        orderOfTraversalAfterBasicTSPHeurisic.get(firstEdgeDest)) +
                        distanceCalculator.getDistance(orderOfTraversalAfterBasicTSPHeurisic.get(secondEdgeSrc),
                                orderOfTraversalAfterBasicTSPHeurisic.get(secondEdgeSrc+1));
                double totalEdgePairLengthAfterSwapping =
                        distanceCalculator.getDistance(orderOfTraversalAfterBasicTSPHeurisic.get(firstEdgeDest-1),
                                orderOfTraversalAfterBasicTSPHeurisic.get(secondEdgeSrc)) +
                                distanceCalculator.getDistance(orderOfTraversalAfterBasicTSPHeurisic.get(firstEdgeDest),
                                        orderOfTraversalAfterBasicTSPHeurisic.get(secondEdgeSrc+1));
                if(totalEdgePairLengthAfterSwapping<currentEdgePairTotalLength){
                    //swap
                    ArrayList<Attraction> newOrderAfeterPartial2opt = new ArrayList<Attraction>();
                    newOrderAfeterPartial2opt.addAll(orderOfTraversalAfterBasicTSPHeurisic.subList(0,firstEdgeDest));
                    List<Attraction> subListBetweenTheTwoEdges = orderOfTraversalAfterBasicTSPHeurisic.subList(firstEdgeDest, secondEdgeSrc + 1);
                    Collections.reverse(subListBetweenTheTwoEdges);
                    newOrderAfeterPartial2opt.addAll(subListBetweenTheTwoEdges);
                    newOrderAfeterPartial2opt.addAll(orderOfTraversalAfterBasicTSPHeurisic.subList(secondEdgeSrc+1, noOfAttractions));
                    orderOfTraversalAfterBasicTSPHeurisic = newOrderAfeterPartial2opt;
                    secondEdgeSrc = firstEdgeDest+1;
                }
                else {
                    secondEdgeSrc++;
                }
            }
            firstEdgeDest++;
        }
        return orderOfTraversalAfterBasicTSPHeurisic;
    }

    public static ArrayList<Attraction> TSPSolverForAttractions(Collection<Attraction> attractionsToVisit, Attraction lastVisitedAttraction, DistanceCalculator<Attraction> distanceCalculator) {
        ArrayList<Attraction> listOfUnvisitedAttractions
                = new ArrayList<Attraction>(attractionsToVisit);
        ArrayList<Attraction> orderOfTraversal = new ArrayList<Attraction>();
        orderOfTraversal.add(lastVisitedAttraction);
        listOfUnvisitedAttractions.remove(lastVisitedAttraction);
        while (!listOfUnvisitedAttractions.isEmpty()) {
            double distanceFromClosestAttractionSeenSoFar = -1;
            Attraction closestAttraction = null;
            for (Attraction attraction:listOfUnvisitedAttractions){
                if(attraction.equals(lastVisitedAttraction)){
                    continue;
                }
                else {
                    if(distanceFromClosestAttractionSeenSoFar==-1){
                        distanceFromClosestAttractionSeenSoFar = distanceCalculator.getDistance(lastVisitedAttraction,attraction);

                        closestAttraction = attraction;
                    }
                    else {
                       double distanceFromSrc = distanceFromClosestAttractionSeenSoFar = distanceCalculator.getDistance(lastVisitedAttraction, attraction);
                        if(distanceFromClosestAttractionSeenSoFar>distanceFromSrc){
                            distanceFromClosestAttractionSeenSoFar = distanceFromSrc;
                            closestAttraction = attraction;
                        }
                    }
                }
            }
            if(closestAttraction!=null) {
                lastVisitedAttraction = closestAttraction;
                orderOfTraversal.add(closestAttraction);
//                System.out.print("'"+Double.toString(closestAttraction.getLatitude())+","+Double.toString(closestAttraction.getLongitude()));


            }
            listOfUnvisitedAttractions.remove(lastVisitedAttraction);
        }
        return orderOfTraversal;
    }

    public static Double getTotalTimeSpentOnADay(DistanceCalculator<Attraction> distanceCalculator, List<Attraction> attractionsForTheDay) {
        Double totalTimeSpent = 0.0;
        Attraction prevAttraction = null;
        for (Attraction curAttraction : attractionsForTheDay){
            if(prevAttraction!=null){
                totalTimeSpent += distanceCalculator.getDistance(prevAttraction, curAttraction);
            }
            totalTimeSpent+= curAttraction.getVisitTime();
            prevAttraction= curAttraction;
        }
        return totalTimeSpent;
    }

    public static double getTotalTimeSpentInTransitForASchedule(ArrayList<List<Attraction>> schedule, DistanceCalculator<Attraction> distanceCalculator){
        double totalTimeSpent = 0.0;
        for (List<Attraction> scheduleForOneDay:schedule){
            Attraction prevAttraction = null;
            for (Attraction curAttraction :scheduleForOneDay){
                if (prevAttraction!=null){
                    totalTimeSpent+= distanceCalculator.getDistance(prevAttraction,curAttraction);
                }
                prevAttraction = curAttraction;
            }
        }
        return totalTimeSpent;
    }

    public static Set<Attraction> getExtremeAttraction(ArrayList<Attraction> listOfAttractions) {
        Attraction westernmostAttraction = null;
        Attraction easternmostAttraction = null;
        Attraction northernmostAttraction = null;
        Attraction southernmostAttraction = null;

        Set<Attraction> extremeAttractions = new HashSet<Attraction>();
        for (Attraction attraction:listOfAttractions){
            if(westernmostAttraction==null||westernmostAttraction.getLongitude()>attraction.getLongitude()){
                westernmostAttraction=attraction;
            }
            else if(easternmostAttraction==null||easternmostAttraction.getLongitude()<attraction.getLongitude()){
                easternmostAttraction=attraction;
            }
            if(southernmostAttraction==null||southernmostAttraction.getLatitude()>attraction.getLatitude()){
                southernmostAttraction=attraction;
            }
            else if(northernmostAttraction==null||northernmostAttraction.getLatitude()<attraction.getLatitude()){
                northernmostAttraction=attraction;
            }
        }
        extremeAttractions.add(westernmostAttraction);
//        extremeAttractions.add(easternmostAttraction);
//        extremeAttractions.add(southernmostAttraction);
//        extremeAttractions.add(northernmostAttraction);
        return extremeAttractions;
    }
}
