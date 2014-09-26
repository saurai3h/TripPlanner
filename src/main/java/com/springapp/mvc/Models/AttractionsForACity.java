package com.springapp.mvc.Models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Saurabh Paliwal on 25/9/14.
 */
public class AttractionsForACity {

    public static ArrayList<String> attractionsForACity(String city) {
        try {

            Integer cityID = findCityID(city);

            ArrayList<String> attractionNames = new ArrayList<String>();

                Connection conn = SqlConnection.getConnection();

                PreparedStatement findAttractionsStatement;
                findAttractionsStatement = conn.prepareStatement(
                        "select attractionName from attractionmapping where cityID = ?");
                findAttractionsStatement.setInt(1, cityID);

                ResultSet resultSet = findAttractionsStatement.executeQuery();

                while(resultSet.next()) {
                    String attractionName = resultSet.getString("attractionName");
                    attractionNames.add(attractionName);
                }

                conn.close();
                findAttractionsStatement.close();


            return attractionNames;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer findCityID(String cityName)   {

        try {

            Integer cityID = null;
            Connection conn = SqlConnection.getConnection();

            PreparedStatement cityNameStatement;
            cityNameStatement = conn.prepareStatement(
                    "select cityID from citymapping where cityName = ?");
            cityNameStatement.setString(1, cityName);

            ResultSet resultSet = cityNameStatement.executeQuery();

            while (resultSet.next()) {
                cityID = resultSet.getInt("cityID");
            }

            conn.close();
            cityNameStatement.close();

            return cityID;
        }
        catch (SQLException s)  {
            s.printStackTrace();
            return null;
        }
    }
}
