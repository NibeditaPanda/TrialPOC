package com.tesco.services;

import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.healthChecks.ServiceHealthCheck;
import com.tesco.services.resources.*;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class Controller extends Service<Configuration> {

    public static void main(String[] args) throws Exception {
        new Controller().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.setName("Price Service");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.addResource(new PriceResource(new PriceDAO(configuration)));
        environment.addResource(new VersionResource());
        environment.addResource(new RootResource());
        environment.addResource(new ImportResource(configuration, new RuntimeWrapper()));
        environment.addHealthCheck(new ServiceHealthCheck(configuration));
    }
}
