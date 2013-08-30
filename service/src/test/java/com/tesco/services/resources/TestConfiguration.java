package com.tesco.services.resources;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.tesco.services.Configuration;
import com.tesco.services.HostedGraphiteConfiguration;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TestConfiguration extends Configuration {

    private Map<String,Object> configuration = null;

    public TestConfiguration(){
        Yaml yamlConfiguration = new Yaml();
        try {
            String app_env = System.getProperty("environment");
            String filename = "ci".equalsIgnoreCase(app_env) ? "ci.yml" : "test.yml";
            configuration = (Map<String,Object>) yamlConfiguration.load(new String(Files.readAllBytes(Paths.get(filename))));
        } catch (IOException exception) {
            LoggerFactory.getLogger(TestConfiguration.class).error(exception.getMessage());
        }
    }

    @Override
    public int getDBPort() {
        return (Integer) configuration.get("DBPort");
    }

    @Override
    public String getDBName() {
        return (String) configuration.get("DBName");
    }

    @Override
    public String getDBHost() {
        return (String) configuration.get("DBHost");
    }

    @Override
    public String getUsername() {
        String username = (String) configuration.get("Username");
        return username == null ? "" : username;
    }

    @Override
    public String getPassword() {
        String password = (String) configuration.get("Password");
        return password == null ? "" : password;
    }

    @Override
    public HostedGraphiteConfiguration getHostedGraphiteConfig () {
        return new HostedGraphiteConfiguration("carbon.hostedgraphite.com",2003,5,"");
    }

}
