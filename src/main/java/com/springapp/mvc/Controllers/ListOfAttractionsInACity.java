package com.springapp.mvc.Controllers;

import com.springapp.mvc.Models.AttractionsForACity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

/**
 * Created by Saurabh Paliwal on 25/9/14.
 */
public class ListOfAttractionsInACity {

    @RequestMapping(value = "/api/attractionsForACity", method = RequestMethod.POST)
    @ResponseBody
    public String sendAttractions(@RequestParam("city")String city, @RequestParam("days")Integer days, ModelMap model) throws JSONException {

        ArrayList<String> attractions = AttractionsForACity.attractionsForACity(city);
        JSONArray jsonArray = new JSONArray();
        for(String attraction : attractions) {
            jsonArray.put(attraction);
        }
        return jsonArray.toString();
    }

}
