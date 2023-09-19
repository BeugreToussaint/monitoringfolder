/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbs.monitoringtransfert.utility;

import com.sbs.monitoringtransfert.config.ConfigProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 *
 * @author Tuxbe
 * @Date 21/07/2023
 */
public class logUtility {

    public static Properties config = ConfigProperties.loadConfig();


    public static void configureLogging(String logDirectory) {
        try {
            Path logDirPath = Paths.get(logDirectory);
            if (!Files.exists(logDirPath)) {
                Files.createDirectories(logDirPath);
            }
            Path logFile = Paths.get(logDirectory+"/application.log");
            if (Files.exists(logFile)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
                Files.copy(logFile, Paths.get(logDirectory + "/application_"+dateFormat.format(new Date()) + ".log"));
            }
            
            String logFilePath = logDirectory + "/application.log";
            Handler fileHandler = new FileHandler(logFilePath);
            fileHandler.setFormatter(new SimpleFormatter());

            Logger logger = Logger.getLogger("");
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
