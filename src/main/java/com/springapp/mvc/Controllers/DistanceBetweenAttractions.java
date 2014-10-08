package com.springapp.mvc.Controllers;

import com.google.gson.Gson;
import com.springapp.mvc.AttractionSelectionAlgo.AttractionSelector;
import com.springapp.mvc.AttractionSelectionAlgo.AttractionSelectorSimple;
import com.springapp.mvc.AttractionSelectionAlgo.GratificationScoreCalculatorSimple;
import com.springapp.mvc.Models.Attraction;
import com.springapp.mvc.Models.SqlQueryExecutor;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Saurabh Paliwal on 1/10/14.
 */

@Controller
@RequestMapping("/")
public class DistanceBetweenAttractions {

    @RequestMapping(value = "/api/distanceBetweenAttractions", method = RequestMethod.GET)
    @ResponseBody
    public String distanceBetweenTwoID(@RequestParam String attractionIDOne, @RequestParam String attractionIDTwo, ModelMap model) throws JSONException,SQLException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("distance",SqlQueryExecutor.getDistanceBetweenAttractions(Integer.parseInt(attractionIDOne),Integer.parseInt(attractionIDTwo)));
        return jsonObject.toString();
    }


        @RequestMapping(value = "/api/distanceBetweenAllAttractions", method = RequestMethod.POST)
        @ResponseBody
        public String distanceBetweenAllAttractions(@RequestParam String attractionIDs, ModelMap model) throws SQLException {

            JSONParser parser=new JSONParser();
            ArrayList<ArrayList<Double>> listOfDistances = new ArrayList<ArrayList<Double>>();
            try {
                JSONArray dayScheduleArray = (JSONArray) parser.parse(attractionIDs);
                for(Object daySchedule:dayScheduleArray){
                    JSONArray attractionArray = (JSONArray) daySchedule;
                    int prevAttractionId = -1;
                    ArrayList<Double> listOfDistancesForThisDay = new ArrayList<Double>();
                    for (Object attractionIDJsonObject:attractionArray){
                        Integer currentAttractionId = Integer.parseInt(attractionIDJsonObject.toString());
                        if(prevAttractionId != -1){
                            listOfDistancesForThisDay.add(SqlQueryExecutor.getDistanceBetweenAttractions(prevAttractionId,currentAttractionId));
                        }
                        prevAttractionId = currentAttractionId;
                    }
                    listOfDistances.add(listOfDistancesForThisDay);
                }
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(listOfDistances);
                return jsonArray.toJSONString();

            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
}
