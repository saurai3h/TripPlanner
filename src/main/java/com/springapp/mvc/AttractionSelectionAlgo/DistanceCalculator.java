package com.springapp.mvc.AttractionSelectionAlgo;

/**
 * Created by kartik.k on 10/9/2014.
 */
public interface DistanceCalculator <T>{
    public double getDistance(T src, T dest);
}
