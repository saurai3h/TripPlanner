package com.springapp.mvc.Models;

import com.springapp.mvc.Utility.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Saurabh Paliwal on 25/9/14.
 */
public class SqlConnection {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://" + Constants.HOST + "/tripplanner";

    static final String USER = "root";
    static final String PASS = "password";

    public static Connection getConnection(){
        try {

            Class.forName(JDBC_DRIVER);
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
            return connection;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


}
