package com.springapp.mvc.Models;

import com.springapp.mvc.AttractionSelectionAlgo.DistanceCalculator;
import com.springapp.mvc.AttractionSelectionAlgo.Trip;
import com.springapp.mvc.Utility.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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
    public static final String TRIPPLANNER_ATTRACTION_MAPPING = "tripplanner.attractionmapping";
    private static final String BEST_TRIPS_STEP_FUNCTION_CORNER_CACHE = "besttripsstepfunctioncornercache";
    private static final String TRIPPLANNER_ATTRACTION_DETAIL = "tripplanner.attractiondetail";
    private static final String TRIPPLANNER_ATTRACTION_CATEGORY_MAPPING = "tripplanner.attractioncategorymapping";
    private static final String DISTANCE_BETWEEN_ATTRACTIONS = "distancebetweenattractions";
    private static final String CITY_MAPPING = "citymapping";
    private static Map<String,DistanceCalculator<Attraction>> cityDistanceCalculatorMapCached = new HashMap<String, DistanceCalculator<Attraction>>();
    private static Map<String,ArrayList<Attraction>> listOfAttractionsCached = new HashMap<String, ArrayList<Attraction>>();

    public static Connection getConnection() {
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

        PreparedStatement findDistanceStatement = connection.prepareStatement(
                "SELECT distance FROM " + DISTANCE_BETWEEN_ATTRACTIONS + " WHERE attractionIDFirst = ? AND attractionIDSecond = ?");
        findDistanceStatement.setInt(1, firstID);
        findDistanceStatement.setInt(2, secondID);
        ResultSet distanceSet = findDistanceStatement.executeQuery();
        Double distance = new Double(0.0);
        if (distanceSet.next()) {
            distance = distanceSet.getDouble("distance");
        }
        findDistanceStatement.close();
        connection.close();
        return distance;
    }

    public static int getCityIdByName(String cityName) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement selectCityIdStatement = connection.prepareStatement(
                "SELECT cityID FROM " + CITY_MAPPING + " WHERE cityName = ?");
        selectCityIdStatement.setString(1, cityName);
        ResultSet cityIdSet = selectCityIdStatement.executeQuery();
        int cityID = -1;
        if (cityIdSet.next()) {
            cityID = cityIdSet.getInt("cityID");
        }
        selectCityIdStatement.close();
        connection.close();
        return cityID;
    }

    public static int getAttractionIdByName(String attractionName) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement selectAttractionIdStatement = connection.prepareStatement(
                "SELECT attractionID FROM " + TRIPPLANNER_ATTRACTION_MAPPING +
                        " WHERE attractionName = ?");
        selectAttractionIdStatement.setString(1, attractionName);
        ResultSet attractionIdSet = selectAttractionIdStatement.executeQuery();
        int attractionID = -1;
        if (attractionIdSet.next()) {
            attractionID = attractionIdSet.getInt("attractionID");
        }
        selectAttractionIdStatement.close();
        connection.close();
        return attractionID;
    }

    public static ArrayList<Attraction> getAllAttractionsForACity(String city) {
        if (listOfAttractionsCached.containsKey(city)) {
            return listOfAttractionsCached.get(city);
        } else {
            try {

                Integer cityID = getCityIdByName(city);

                //ArrayList<Attraction> attractionNames = new ArrayList<Attraction>();
                ArrayList<Attraction> attractionList = new ArrayList<Attraction>();

                Connection conn = getConnection();

                PreparedStatement findAttractionsStatement;
                findAttractionsStatement = conn.prepareStatement(
                        "SELECT " + TRIPPLANNER_ATTRACTION_CATEGORY_MAPPING + ".category, " + TRIPPLANNER_ATTRACTION_MAPPING + ".attractionName," +
                                TRIPPLANNER_ATTRACTION_MAPPING +
                                ".attractionID," +
                                "  " + TRIPPLANNER_ATTRACTION_DETAIL + ".noOfReviews, " + SqlQueryExecutor.TRIPPLANNER_ATTRACTION_DETAIL + ".noOfStars," +
                                "  " + TRIPPLANNER_ATTRACTION_DETAIL + ".attractionReviewURL," + SqlQueryExecutor.TRIPPLANNER_ATTRACTION_DETAIL + ".attractionType," +
                                "  " + TRIPPLANNER_ATTRACTION_DETAIL + ".attractionFee," + SqlQueryExecutor.TRIPPLANNER_ATTRACTION_DETAIL + ".attractionVisitTime," +
                                "  " + TRIPPLANNER_ATTRACTION_DETAIL + ".attractionDescription," + SqlQueryExecutor.TRIPPLANNER_ATTRACTION_DETAIL + ".attractionLongitude," +
                                "  " + TRIPPLANNER_ATTRACTION_DETAIL + ".attractionLatitude," + SqlQueryExecutor.TRIPPLANNER_ATTRACTION_DETAIL + ".attractionImageURL," +
                                "  " + TRIPPLANNER_ATTRACTION_DETAIL + ".additionalInformation," + SqlQueryExecutor.TRIPPLANNER_ATTRACTION_DETAIL + ".activities FROM " +
                                TRIPPLANNER_ATTRACTION_MAPPING + " INNER JOIN " + TRIPPLANNER_ATTRACTION_DETAIL + " " +
                                "ON " + TRIPPLANNER_ATTRACTION_MAPPING + ".attractionID = " +
                                TRIPPLANNER_ATTRACTION_DETAIL + ".attractionID INNER JOIN " + TRIPPLANNER_ATTRACTION_CATEGORY_MAPPING + " " +
                                "ON " + TRIPPLANNER_ATTRACTION_MAPPING + ".attractionID = " + TRIPPLANNER_ATTRACTION_CATEGORY_MAPPING + ".attractionID " +
                                "WHERE " + TRIPPLANNER_ATTRACTION_MAPPING + ".cityID = ?;");
                findAttractionsStatement.setInt(1, cityID);

                ResultSet resultSet = findAttractionsStatement.executeQuery();

                while (resultSet.next()) {

                    Attraction attraction = new Attraction();
                    attraction.setCategory(resultSet.getString("category"));
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
                listOfAttractionsCached.clear();
                listOfAttractionsCached.put(city,attractionList);
                return attractionList;

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void storeTripStepFunctionCornerInCache(String cityName, Trip trip) {
        Connection connection = getConnection();

        try {
            PreparedStatement addToCache = connection.prepareStatement(
                    "INSERT INTO " + BEST_TRIPS_STEP_FUNCTION_CORNER_CACHE + " (CityID, TripDuration, AttractionsBitString) VALUES (?,?,?);");
            addToCache.setInt(1, getCityIdByName(cityName));
            addToCache.setDouble(2, trip.getTimeRequired());
            addToCache.setInt(3, trip.getAttractionsVisitedBitArray());
            addToCache.execute();
            addToCache.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static TreeMap<Double, Integer> getDurationBitStringMapForCornerTrips(String cityName, Double minTripLength, Double maxTripLength) {
        TreeMap<Double, Integer> durationBitStringMap = new TreeMap<Double, Integer>();
        Connection connection = getConnection();

        try {
            PreparedStatement getCornerTrips = connection.prepareStatement(
                    "SELECT TripDuration, AttractionsBitString FROM  " + BEST_TRIPS_STEP_FUNCTION_CORNER_CACHE + " WHERE CityID = ?" +
                            " AND TripDuration>? AND TripDuration<?");
            getCornerTrips.setInt(1, getCityIdByName(cityName));
            getCornerTrips.setDouble(2, minTripLength-5);
            getCornerTrips.setDouble(3,maxTripLength+5);
            ResultSet trips = getCornerTrips.executeQuery();
            while (trips.next()) {
                Integer tripBitString = trips.getInt("AttractionsBitString");
                Double tripDuration = trips.getDouble("TripDuration");
                durationBitStringMap.put(tripDuration, tripBitString);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return durationBitStringMap;
    }

    public static DistanceCalculator<Attraction> getDistanceMatrix(String cityName) {
        if(cityDistanceCalculatorMapCached.containsKey(cityName)){
            return cityDistanceCalculatorMapCached.get(cityName);
        }
        else {
            final ArrayList<Attraction> listOfAttractions = getAllAttractionsForACity(cityName);
            Map<Integer, Integer> mapBetweenIndexInListOfAttractionAndID = new HashMap<Integer, Integer>();
            for (int i = 0; i < listOfAttractions.size(); i++) {
                Attraction attraction = listOfAttractions.get(i);
                mapBetweenIndexInListOfAttractionAndID.put(attraction.getAttractionID(), i);
            }
            int noOfAttractions = listOfAttractions.size();
            final Double[][] distanceMatrix = new Double[noOfAttractions][noOfAttractions];
            Connection connection = getConnection();
            try {
                PreparedStatement getAllDistancesStatement = connection.prepareStatement("SELECT *" +
                        "FROM distancebetweenattractions WHERE cityID=? " +
                        "AND attractionIDFirst<attractionIDSecond");
                getAllDistancesStatement.setInt(1, getCityIdByName(cityName));
                ResultSet resultSet = getAllDistancesStatement.executeQuery();
                while (resultSet.next()) {
                    int firstAttraction = mapBetweenIndexInListOfAttractionAndID.get(resultSet.getInt("attractionIDFirst"));
                    int secondAttraction = mapBetweenIndexInListOfAttractionAndID.get(resultSet.getInt("attractionIDSecond"));
                    double distance = resultSet.getDouble("distance");
                    distanceMatrix[firstAttraction][secondAttraction] = distance;
                    distanceMatrix[secondAttraction][firstAttraction] = distance;
                }
                for (int attractionNo = 0; attractionNo < noOfAttractions; attractionNo++) {
                    distanceMatrix[attractionNo][attractionNo] = 0.0;
                }
                connection.close();
                DistanceCalculator<Attraction> distanceCalculator = new DistanceCalculator<Attraction>() {
                    @Override
                    public double getDistance(Attraction src, Attraction dest) {
                        return distanceMatrix[listOfAttractions.indexOf(src)][listOfAttractions.indexOf(dest)] / 30000;
                    }
                };
                cityDistanceCalculatorMapCached.put(cityName, distanceCalculator);
                return distanceCalculator;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}