package com.springapp.mvc.Controllers;

import com.springapp.mvc.Models.AttractionsForACity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

        ArrayList<String> attractions = AttractionsForACity.attractionsForACity(city);

        JSONObject attractionObject = new JSONObject();
        JSONArray attractionObjects = new JSONArray();

        for(String attraction : attractions) {

            attractionObject.put("attractionName", attraction);
            attractionObject.put("attractionImage",);

            attractionObjects.put(attractionObject);
        }
    return attractionObjects.toString();

    }
}