package com.springapp.mvc.Controllers;

import com.springapp.mvc.AttractionSelectionAlgo.AttractionSelector;
import com.springapp.mvc.AttractionSelectionAlgo.AttractionSelectorSimple;
import com.springapp.mvc.AttractionSelectionAlgo.GratificationScoreCalculatorSimple;
import com.springapp.mvc.Models.Attraction;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * Created by Saurabh Paliwal on 25/9/14.
 */

@Controller
@RequestMapping("/")
public class ListOfAttractionsInACity {

    @RequestMapping(value = "/api/attractionsForACity", method = RequestMethod.GET)
    @ResponseBody
    public String sendAttractions(@RequestParam String city,@RequestParam String days, ModelMap model) throws JSONException{

        Integer numberOfDays = Integer.parseInt(days);


        JSONArray scheduleForAllDays = new JSONArray();
        AttractionSelector attractionSelector = new AttractionSelectorSimple();
        ArrayList<ArrayList<Attraction>> listOfSchedules = attractionSelector.selectAttraction(new GratificationScoreCalculatorSimple(), city, numberOfDays);
        for(ArrayList<Attraction> scheduleOfOneDay:listOfSchedules) {
            JSONArray oneDayScheduleJSON = new JSONArray();
            for(Attraction attraction:scheduleOfOneDay){
                oneDayScheduleJSON.put(attraction.getName());
            }
            scheduleForAllDays.put(oneDayScheduleJSON);
        }
    return scheduleForAllDays.toString();

    }
}