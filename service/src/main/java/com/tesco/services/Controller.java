package com.tesco.services;

import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.DAO.PromotionDAO;
import com.tesco.services.healthChecks.ServiceHealthCheck;
import com.tesco.services.metrics.ResourceMetricsListener;
import com.tesco.services.resources.*;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.reporting.ConsoleReporter;
import com.yammer.metrics.reporting.GraphiteReporter;

import java.util.concurrent.TimeUnit;

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
        environment.addResource(new PromotionResource(new PromotionDAO(configuration)));
        environment.addResource(new VersionResource());
        environment.addResource(new RootResource());
        environment.addResource(new ImportResource(configuration, new RuntimeWrapper()));
        environment.addHealthCheck(new ServiceHealthCheck(configuration));

        ResourceMetricsListener metricsListener = new ResourceMetricsListener();
        Metrics.defaultRegistry().addListener(metricsListener);
        HostedGraphiteConfiguration hostedGraphiteConfig = configuration.getHostedGraphiteConfig();
        GraphiteReporter.enable(metricsListener.getRegistry(), hostedGraphiteConfig.getPeriod(), TimeUnit.SECONDS, hostedGraphiteConfig.getHostname(), hostedGraphiteConfig.getPort(), hostedGraphiteConfig.getApikey());
//        ConsoleReporter.enable(metricsListener.getRegistry(),5,TimeUnit.SECONDS);
    }
}
