package com.tesco.services;

public class TestConfiguration extends Configuration {
    @Override
    public int getDBPort() {
        String app_env = System.getProperty("environment");
        if("ci".equalsIgnoreCase(app_env)) {
            return 10093;
        }
        return 27017;
    }

    @Override
    public String getDBName() {
        String app_env = System.getProperty("environment");
        if("ci".equalsIgnoreCase(app_env)) {
            return "KaYIvBPHSZ3OAsmo4lGAQ";
        }
        return "testPriceService";
    }

    @Override
    public String getDBHost() {
        String app_env = System.getProperty("environment");
        if("ci".equalsIgnoreCase(app_env)) {
            return "linus.mongohq.com";
        }
        return "localhost";
    }

    @Override
    public String getUsername() {
        String app_env = System.getProperty("environment");
        if("ci".equalsIgnoreCase(app_env)) {
            return "cloudbees";
        }
        return "";
    }

    @Override
    public String getPassword() {
        String app_env = System.getProperty("environment");
        if("ci".equalsIgnoreCase(app_env)) {
            return "56dcbcf21addbe3a4369b331240d479f";
        }
        return "";
    }
}
