package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;
import com.springapp.mvc.Utility.Constants;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

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
            if (!cityName.equals("Dubai"))
                continue;
            System.out.println("caching " + cityName);
            new BestPossibleSubsetCacher(cityName, new GratificationScoreCalculatorSimple()).tripsSortedByTimeTakenAsc();
        }

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

    @Test
    public void shouldFindTimeSpentOnADayCorrectly(){
        AttractionSelectorBestSubsetOfAttractions attractionSelector = new AttractionSelectorBestSubsetOfAttractions(new GratificationScoreCalculatorSimple());
        ArrayList<Attraction> attractionArrayList = new ArrayList<Attraction>();
        for (int i = 0; i < 4; i++) {
            Attraction a=new Attraction();
            a.setVisitTime(50*i);
            attractionArrayList.add(a);
        }
        Double totalTimeSpentOnADay = TSPSolverHeuristicsHelper.getTotalTimeSpentOnADay(
                new DistanceCalculator<Attraction>() {
                    @Override
                    public double getDistance(Attraction src, Attraction dest) {
                        return 10;
                    }
                }, attractionArrayList);
        Assert.assertTrue("visitTime Good",totalTimeSpentOnADay==330);
    }

    @Test
    public void distanceCalculator(){
        long startTime = new Date().getTime();
        DistanceCalculator<Attraction> distanceCalculator =  SqlQueryExecutor.getDistanceMatrix("Mumbai");


        ArrayList<Attraction> attractionsInMumbai = SqlQueryExecutor.getAllAttractionsForACity("Mumbai");
        for(int i = 0;i<30;i++){
            for(int j=0;j<30;j++){
                distanceCalculator.getDistance(attractionsInMumbai.get(i),attractionsInMumbai.get(j));
            }
        }
        System.out.println((new Date().getTime() - startTime));
    }

}