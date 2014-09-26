package com.springapp.mvc.Models;

import com.springapp.mvc.Utility.Constants;
import java.sql.*;
import java.util.ArrayList;

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
    public static int getCityIdByName(String cityName) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement selectCityIdStatement=connection.prepareStatement(
                "SELECT cityID FROM cityMapping WHERE cityName = ?");
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
                "SELECT attractionID FROM attractionMapping WHERE attractionName = ?");
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

    public static void cleanAllTables(){
        try {
            Connection connection = getConnection();
            PreparedStatement cleanAttractionDetails=connection.prepareStatement(
                    "DELETE FROM attractiondetail");
            PreparedStatement cleanAttractions=connection.prepareStatement(
                    "DELETE FROM attractionMapping");
            PreparedStatement cleanAttractionCities=connection.prepareStatement(
                    "DELETE FROM cityMapping");
            cleanAttractionDetails.execute();
            cleanAttractions.execute();
            cleanAttractionCities.execute();

            cleanAttractionCities = connection.prepareStatement(
                    "ALTER TABLE cityMapping AUTO_INCREMENT = 1");
            cleanAttractions = connection.prepareStatement(
                    "ALTER TABLE attractionMapping AUTO_INCREMENT = 1");

            cleanAttractionCities.execute();
            cleanAttractions.execute();

            cleanAttractionDetails.close();
            cleanAttractions.close();
            cleanAttractionCities.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Attraction> getAllAttractionsForACity(String city) {
        try {

            Integer cityID = getCityIdByName(city);

            ArrayList<Attraction> attractionList = new ArrayList<Attraction>();

                Connection conn = getConnection();

                PreparedStatement findAttractionsStatement;
                findAttractionsStatement = conn.prepareStatement(
                        "SELECT tripplanner.attractionmapping.attractionName,tripplanner.attractionmapping.attractionID," +
                                "  tripplanner.attractiondetail.noOfReviews, tripplanner.attractiondetail.noOfStars," +
                                "  tripplanner.attractiondetail.attractionReviewURL,tripplanner.attractiondetail.attractionType," +
                                "  tripplanner.attractiondetail.attractionFee,tripplanner.attractiondetail.attractionVisitTime," +
                                "  tripplanner.attractiondetail.attractionDescription,tripplanner.attractiondetail.attractionLatitude," +
                                "  tripplanner.attractiondetail.attractionLongitude,tripplanner.attractiondetail.attractionImageURL," +
                                "  tripplanner.attractiondetail.additionalInformation,tripplanner.attractiondetail.activities FROM " +
                                "tripplanner.attractionmapping INNER JOIN tripplanner.attractiondetail " +
                                "ON tripplanner.attractionmapping.attractionID = " +
                                "tripplanner.attractiondetail.attractionID WHERE tripplanner.attractionmapping.cityID = ?;");
                findAttractionsStatement.setInt(1, cityID);

                ResultSet resultSet = findAttractionsStatement.executeQuery();

                while(resultSet.next()) {
                    Attraction attraction = new Attraction(
                            resultSet.getInt("noOfReviews"),resultSet.getFloat("noOfStars"),
                            resultSet.getString("attractionName"),city
                    );
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
}