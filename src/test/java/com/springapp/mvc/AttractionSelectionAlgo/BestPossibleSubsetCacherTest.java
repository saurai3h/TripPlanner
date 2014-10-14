package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;
import com.springapp.mvc.Utility.Constants;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.TreeMap;

public class BestPossibleSubsetCacherTest {
    BestPossibleSubsetCacher bestPossibleSubsetCacher;
    @Before
    public void setUp() throws Exception {
        bestPossibleSubsetCacher = new BestPossibleSubsetCacher("Paris",new GratificationScoreCalculatorSimple());

    }

    @Test
    public void testGratificationScoreCalculator(){
        System.out.println(bestPossibleSubsetCacher.getGratificationScoreFromBitString((long) (7*Math.pow(2,13))));
    }

    @Test
    public void testGetBestTrips() throws Exception {
        String[] list_of_cities = Constants.LIST_OF_CITIES;
        for (int i = 0; i < list_of_cities.length; i++) {
            String cityName = list_of_cities[i];
            if (!cityName.equals("Sydney"))
                continue;
            System.out.println("caching " + cityName);
            new BestPossibleSubsetCacher(cityName, new GratificationScoreCalculatorSimple()).tripsSortedByTimeTakenAsc();
        }

    }

    @Test
    public void testGetDistanceBasedOnLatlong() throws Exception {
        System.out.println(bestPossibleSubsetCacher.getDistance(1, 2));

    }

    @Test
    public void testEstimateTimeForOneSubset() throws Exception{
        System.out.println(bestPossibleSubsetCacher.getMinTimeRequiredToVisitGivenAttractions(
                new HashSet<Attraction>( SqlQueryExecutor.getAllAttractionsForACity("Paris"))
        ));
    }

    @Test
    public void testAddToCache(){
        SqlQueryExecutor.storeTripStepFunctionCornerInCache("Istanbul",new Trip(1022,1000,48));
    }
}