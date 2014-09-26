package com.springapp.mvc.Controllers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class HelloController {
	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {
		model.addAttribute("message", "Hello World!");
		return "index";
	}


    @RequestMapping(value = "/api/cities", method = RequestMethod.GET)
    @ResponseBody
    public String sendJson(ModelMap model) throws JSONException {
        String[] cities = {"Bangkok", "Seoul", "London", "Milan", "Paris", "Rome", "Singapore", "Shanghai" , "New York", "Amsterdam", "Istanbul", "Tokyo", "Dubai", "Vienna", "Kuala Lumpur", "Taipei", "Hong Kong", "Riyadh", "Barcelona", "Los Angeles"};
        JSONArray jsonArray = new JSONArray();
        for(String city : cities) {
            JSONObject object = new JSONObject();
            object.put("cityName",city);
            jsonArray.put(object);
        }
        return jsonArray.toString();
    }
}

