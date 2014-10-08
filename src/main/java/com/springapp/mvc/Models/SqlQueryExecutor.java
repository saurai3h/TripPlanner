package com.springapp.mvc.Models;

import com.springapp.mvc.AttractionSelectionAlgo.Trip;
import com.springapp.mvc.Utility.Constants;
import sun.reflect.generics.tree.Tree;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kartik.k on 9/25/2014.
 */
public class SqlQueryExecutor {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://" + Constants.SQL_DB_HOST + "/tripplanner";

    static final String USER = "root";
    static final String PASS = "password";
    public static Connection getConnection(){
        try {
            Class.forName(JDBC_DRIVER);
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Double getDistanceBetweenAttractions(Integer firstID, Integer secondID) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement findDistanceStatement=connection.prepareStatement(
                "SELECT distance FROM distancebetweenattractions WHERE attractionIDFirst = ? AND attractionIDSecond = ?");
        findDistanceStatement.setInt(1, firstID);
        findDistanceStatement.setInt(2, secondID);
        ResultSet distanceSet = findDistanceStatement.executeQuery();
        Double distance = new Double(0.0);
        if(distanceSet.next()){
            distance = distanceSet.getDouble("distance");
        }
        findDistanceStatement.close();
        connection.close();
        return distance;
    }

    public static int getCityIdByName(String cityName) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement selectCityIdStatement=connection.prepareStatement(
                "SELECT cityID FROM citymapping WHERE cityName = ?");
        selectCityIdStatement.setString(1,cityName);
        ResultSet cityIdSet = selectCityIdStatement.executeQuery();
        int cityID = -1;
        if(cityIdSet.next()){
            cityID = cityIdSet.getInt("cityID");
        }
        selectCityIdStatement.close();
        connection.close();
        return cityID;
    }

    public static int getAttractionIdByName(String attractionName) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement selectAttractionIdStatement=connection.prepareStatement(
                "SELECT attractionID FROM attractionmapping WHERE attractionName = ?");
        selectAttractionIdStatement.setString(1, attractionName);
        ResultSet attractionIdSet = selectAttractionIdStatement.executeQuery();
        int attractionID = -1;
        if(attractionIdSet.next()){
            attractionID = attractionIdSet.getInt("attractionID");
        }
        selectAttractionIdStatement.close();
        connection.close();
        return attractionID;
    }

    public static ArrayList<Attraction> getAllAttractionsForACity(String city) {
        try {

            Integer cityID = getCityIdByName(city);

            //ArrayList<Attraction> attractionNames = new ArrayList<Attraction>();
            ArrayList<Attraction> attractionList = new ArrayList<Attraction>();

                Connection conn = getConnection();

                PreparedStatement findAttractionsStatement;
                findAttractionsStatement = conn.prepareStatement(
                        "SELECT tripplanner.attractionmapping.attractionName,tripplanner.attractionmapping.attractionID," +
                                "  tripplanner.attractiondetail.noOfReviews, tripplanner.attractiondetail.noOfStars," +
                                "  tripplanner.attractiondetail.attractionReviewURL,tripplanner.attractiondetail.attractionType," +
                                "  tripplanner.attractiondetail.attractionFee,tripplanner.attractiondetail.attractionVisitTime," +
                                "  tripplanner.attractiondetail.attractionDescription,tripplanner.attractiondetail.attractionLongitude," +
                                "  tripplanner.attractiondetail.attractionLatitude,tripplanner.attractiondetail.attractionImageURL," +
                                "  tripplanner.attractiondetail.additionalInformation,tripplanner.attractiondetail.activities FROM " +
                                "tripplanner.attractionmapping INNER JOIN tripplanner.attractiondetail " +
                                "ON tripplanner.attractionmapping.attractionID = " +
                                "tripplanner.attractiondetail.attractionID WHERE tripplanner.attractionmapping.cityID = ?;");
                findAttractionsStatement.setInt(1, cityID);

                ResultSet resultSet = findAttractionsStatement.executeQuery();

                while(resultSet.next()) {

                    Attraction attraction = new Attraction();
                    attraction.setName(resultSet.getString("attractionName"));
                    attraction.setAttractionID(resultSet.getInt("attractionID"));
                    attraction.setNoOfReviews(resultSet.getInt("noOfReviews"));
                    attraction.setNoOfStars(resultSet.getDouble("noOfStars"));
                    attraction.setReviewURL(resultSet.getString("attractionReviewURL"));
                    attraction.setType(resultSet.getString("attractionType"));
                    attraction.setFee(resultSet.getBoolean("attractionFee"));
                    attraction.setVisitTime(resultSet.getDouble("attractionVisitTime"));
                    attraction.setDescription(resultSet.getString("attractionDescription"));
                    attraction.setLongitude(resultSet.getDouble("attractionLongitude"));
                    attraction.setLatitude(resultSet.getDouble("attractionLatitude"));
                    attraction.setImageURL(resultSet.getString("attractionImageURL"));
                    attraction.setAdditionalInformation(resultSet.getString("additionalInformation"));
                    attraction.setActivities(resultSet.getString("activities"));
                    attraction.setCityName(city);
                    attractionList.add(attraction);
                }

                conn.close();
                findAttractionsStatement.close();


            return attractionList;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void storeTripStepFunctionCornerInCache(String cityName,Trip trip){
        Connection connection = getConnection();

        try {
            PreparedStatement addToCache=connection.prepareStatement(
                    "INSERT INTO besttripsstepfunctioncornercache (CityID, TripDuration, AttractionsBitString) VALUES (?,?,?);");
            addToCache.setInt(1,getCityIdByName(cityName));
            addToCache.setDouble(2,trip.getTimeRequired());
            addToCache.setInt(3,trip.getAttractionsVisitedBitArray());
            addToCache.execute();
            addToCache.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static TreeMap<Double,Integer> getDurationBitStringMapForCornerTrips(String cityName){
        TreeMap<Double,Integer> durationBitStringMap = new TreeMap<Double, Integer>();
        Connection connection = getConnection();

        try {
            PreparedStatement getCornerTrips = connection.prepareStatement(
                    "SELECT TripDuration, AttractionsBitString FROM  besttripsstepfunctioncornercache WHERE CityID = ?");
            getCornerTrips.setInt(1,getCityIdByName(cityName));
            ResultSet trips = getCornerTrips.executeQuery();
            while (trips.next()){
                Integer tripBitString = trips.getInt("AttractionsBitString");
                Double tripDuration= trips.getDouble("TripDuration");
                durationBitStringMap.put(tripDuration,tripBitString);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    return durationBitStringMap;
    }
}