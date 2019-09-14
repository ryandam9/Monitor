package com.job.monitoring.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import static com.job.monitoring.utils.AppLogging.logger;

public class Utils {
    /**
     * Loads the Properties file.
     * @return
     */
    public static Properties loadProperties() {
        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream("resources/config.properties"));
        } catch (IOException e) {
            System.out.println("Unable to read config.properties file.");
            e.printStackTrace();
            System.exit(1);
        }

        return prop;
    }

    /**
     * Log an Exception
     * @param e
     */
    public static void logStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        logger.debug(sStackTrace);
    }
}