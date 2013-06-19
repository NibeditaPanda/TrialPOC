package com.tesco.services;

public class TestConfiguration extends Configuration {
    @Override
    public int getDBPort() {
        String app_env = System.getProperty("environment");
        if("ci".equalsIgnoreCase(app_env)) {
            return 10073;
        }
        return 27017;
    }

    @Override
    public String getDBName() {
        String app_env = System.getProperty("environment");
        if("ci".equalsIgnoreCase(app_env)) {
            return "5oCmuVVgkTcse96lhOdmkA";
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
            return "6073eeb96870d332b016680d932748b5";
        }
        return "";
    }
}
