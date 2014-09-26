package com.springapp.mvc.Controllers;

import com.springapp.mvc.AttractionSelectionAlgo.GratificationScoreCalculator;
import com.springapp.mvc.AttractionSelectionAlgo.GratificationScoreCalculatorSimple;
import com.springapp.mvc.Models.SqlQueryExecutor;
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

        ArrayList<String> attractions = SqlQueryExecutor.getAllAttractionsForACity(city);
        JSONArray jsonArray = new JSONArray();
        GratificationScoreCalculator gratificationScoreCalculator = new GratificationScoreCalculatorSimple();

        for(String attraction : attractions) {
            jsonArray.put(attraction);
        }
    return jsonArray.toString();

    }
}