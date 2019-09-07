package com.job.monitoring.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Utils {
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
}