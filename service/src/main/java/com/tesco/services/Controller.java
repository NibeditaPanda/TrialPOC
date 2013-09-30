package com.tesco.services;

import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.DAO.PromotionDAO;
import com.tesco.services.healthChecks.ServiceHealthCheck;
import com.tesco.services.metrics.ResourceMetricsListener;
import com.tesco.services.resources.*;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.HttpConfiguration;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.reporting.GraphiteReporter;

import com.wordnik.swagger.jaxrs.config.*;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.config.*;
import com.wordnik.swagger.reader.*;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import org.eclipse.jetty.servlets.CrossOriginFilter;

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

      configureMetrics(configuration, environment);
      configureSwagger(environment, configuration);
    }

  private void configureMetrics(Configuration configuration, Environment environment) {
    environment.addHealthCheck(new ServiceHealthCheck(configuration));
    ResourceMetricsListener metricsListener = new ResourceMetricsListener();
    Metrics.defaultRegistry().addListener(metricsListener);
    HostedGraphiteConfiguration hostedGraphiteConfig = configuration.getHostedGraphiteConfig();
    GraphiteReporter.enable(metricsListener.getRegistry(), hostedGraphiteConfig.getPeriod(), TimeUnit.SECONDS, hostedGraphiteConfig.getHostname(), hostedGraphiteConfig.getPort(), hostedGraphiteConfig.getApikey());
  }

  private void configureSwagger(Environment environment, Configuration configuration) {
    // Swagger Resource
    environment.addResource(new ApiListingResourceJSON());

    // Swagger providers
    environment.addProvider(new ApiDeclarationProvider());
    environment.addProvider(new ResourceListingProvider());

    // Swagger Scanner, which finds all the resources for @Api Annotations
    ScannerFactory.setScanner(new DefaultJaxrsScanner());

    // Add the reader, which scans the resources and extracts the resource information
    ClassReaders.setReader(new DefaultJaxrsApiReader());

    // Set the swagger config options
    SwaggerConfig config = ConfigFactory.config();
    config.setApiVersion("1.0.1");

    HttpConfiguration httpConfiguration = configuration.getHttpConfiguration();
    config.setBasePath("http://localhost:"+httpConfiguration.getPort());

    environment.addFilter(CrossOriginFilter.class, "/*");
  }
}
