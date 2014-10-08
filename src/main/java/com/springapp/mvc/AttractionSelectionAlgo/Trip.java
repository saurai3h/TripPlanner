package com.springapp.mvc.AttractionSelectionAlgo;

/**
* Created by kartik.k on 10/7/2014.
*/
public class Trip {
     private int attractionsVisitedBitArray;
     private double gratificationScore;
     private double timeRequired;

     public int getAttractionsVisitedBitArray() {
         return attractionsVisitedBitArray;
     }

     public double getGratificationScore() {
         return gratificationScore;
     }

     public double getTimeRequired() {
         return timeRequired;
     }

    Trip(int attractionsVisitedBitArray, double gratificationScore, double timeRequired) {
        this.attractionsVisitedBitArray = attractionsVisitedBitArray;
        this.gratificationScore = gratificationScore;
        this.timeRequired = timeRequired;
    }
 }
