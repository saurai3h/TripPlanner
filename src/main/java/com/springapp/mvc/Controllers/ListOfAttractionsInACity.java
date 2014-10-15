package com.springapp.mvc.Controllers;

import com.springapp.mvc.AttractionSelectionAlgo.*;
import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saurabh Paliwal on 25/9/14.
 */

@Controller
@RequestMapping("/")
public class ListOfAttractionsInACity {

    @RequestMapping(value = "/api/attractionsForACity", method = RequestMethod.GET)
    @ResponseBody
    public String sendAttractions(@RequestParam String city,@RequestParam String days,@RequestParam String mode,ModelMap model) throws JSONException{

        Integer numberOfDays = Integer.parseInt(days);


        JSONArray scheduleForAllDays = new JSONArray();
        AttractionSelector attractionSelector = new AttractionSelectorBestSubsetOfAttractions(new GratificationScoreCalculatorSimple());
        if(numberOfDays<2){
            attractionSelector = new AttractionSelectorSimple(new GratificationScoreCalculatorSimple());
        }
        ArrayList<List<Attraction>> listOfSchedules = attractionSelector.selectAttraction(city, numberOfDays, Integer.parseInt(mode));
        if(listOfSchedules==null||listOfSchedules.size()==0){
            listOfSchedules=new AttractionSelectorSimple(new GratificationScoreCalculatorSimple()).selectAttraction(city,numberOfDays,Integer.parseInt(mode));
        }

        for(List<Attraction> scheduleOfOneDay:listOfSchedules) {

            JSONArray oneDayScheduleJSON = new JSONArray();

            for(Attraction attraction:scheduleOfOneDay){
                JSONObject attractionJson = new JSONObject(attraction);
                oneDayScheduleJSON.put(attractionJson);
            }

            scheduleForAllDays.put(oneDayScheduleJSON);
        }

        return scheduleForAllDays.toString();

    }

    @RequestMapping(value = "/api/getAllAttractions", method = RequestMethod.GET)
    @ResponseBody
    public String sendAllAttractions(@RequestParam String city,ModelMap model) throws JSONException{
        JSONArray listOfAllAttractions = new JSONArray();

        ArrayList<Attraction> listOfAttractions = SqlQueryExecutor.getAllAttractionsForACity(city);
        for(Attraction attraction:listOfAttractions){
            JSONObject attractionJSON = new JSONObject(attraction);
            listOfAllAttractions.put(attractionJSON);
        }

        return listOfAllAttractions.toString();

    }
}