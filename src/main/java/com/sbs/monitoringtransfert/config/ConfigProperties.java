/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbs.monitoringtransfert.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Tuxbe
 * @Date 21/07/2023
 */
public class ConfigProperties {
    
        public static Properties loadConfig() {
        Properties prop = new Properties();
        try (InputStream input = ConfigProperties.class.getClassLoader().getResourceAsStream("config.properties")) {
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
        
}
