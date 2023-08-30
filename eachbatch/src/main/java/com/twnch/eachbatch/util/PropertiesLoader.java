package com.twnch.eachbatch.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties loadProperties(String fileName) {
        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
