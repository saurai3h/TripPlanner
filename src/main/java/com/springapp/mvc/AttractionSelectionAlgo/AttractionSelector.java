package com.springapp.mvc.AttractionSelectionAlgo;

import com.springapp.mvc.Models.Attraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by kartik.k on 9/26/2014.
 */
public abstract class AttractionSelector {
    protected static Logger LOGGER;
    private static void setupLogger() {
        LOGGER = Logger.getLogger(AttractionSelectorBestSubsetOfAttractions.class.getName());
        LOGGER.setLevel(Level.FINEST);
        FileHandler logFileHandler;

        try {

            // This block configure the logger with handler and formatter
            logFileHandler = new FileHandler("C:/Users/kartik.k/Desktop/tripPlannerDocumentation/logs/bestSubsetOfAttractionsSelector.log");
            LOGGER.addHandler(logFileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            logFileHandler.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public AttractionSelector(GratificationScoreCalculator gratificationScoreCalculator) {
        this.gratificationScoreCalculator = gratificationScoreCalculator;
        setupLogger();
    }

    public abstract ArrayList<java.util.List<Attraction>> selectAttraction(String cityName, int noOfDays, int mode);
    protected GratificationScoreCalculator gratificationScoreCalculator;
}
