package com.tesco.services;

import com.tesco.services.repositories.DataGridResource;
import com.tesco.services.repositories.UUIDGenerator;
import com.tesco.services.dao.PriceDAO;
import com.tesco.services.healthChecks.ServiceHealthCheck;
import com.tesco.services.metrics.ResourceMetricsListener;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.resources.*;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
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
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;


public class PriceService extends Service<Configuration> {
    public static void main(String[] args) throws Exception {
        new PriceService().run(args);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.setName("Price Service");
        bootstrap.addBundle(new AssetsBundle("/assets/", "/docs", "index.htm"));
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        MutablePicoContainer container = configureDependencies();

        environment.addResource(new PriceResource(new PriceDAO(configuration)));
        environment.addResource(new PromotionResource(container.getComponent(PromotionRepository.class)));
        environment.addResource(new VersionResource());
        environment.addResource(new ImportResource(configuration, container.getComponent(DataGridResource.class)));

        environment.addProvider(new MongoUnavailableProvider());

        /**
         *   @Toy commented out Graphite because tesco boxes don't have access to the Internet at the moment
         */
//      configureMetrics(configuration, environment);
        configureSwagger(environment, configuration);
    }

    private MutablePicoContainer configureDependencies() {
        MutablePicoContainer container = new DefaultPicoContainer();
        container.addComponent(new DataGridResource());
        container.addComponent(new UUIDGenerator());
        container.addComponent(new PromotionRepository(container.getComponent(UUIDGenerator.class),
                container.getComponent(DataGridResource.class).getPromotionCache()));
        return container;
    }

    private void configureMetrics(Configuration configuration, Environment environment) {
        environment.addHealthCheck(new ServiceHealthCheck(configuration));
        ResourceMetricsListener metricsListener = new ResourceMetricsListener();
        Metrics.defaultRegistry().addListener(metricsListener);
        HostedGraphiteConfiguration hostedGraphiteConfig = configuration.getHostedGraphiteConfig();
        GraphiteReporter.enable(metricsListener.getRegistry(), hostedGraphiteConfig.getPeriod(), TimeUnit.SECONDS, hostedGraphiteConfig.getHostname(), hostedGraphiteConfig.getPort(), hostedGraphiteConfig.getApikey());
    }

    private void configureSwagger(Environment environment, Configuration configuration) throws UnknownHostException, SocketException {
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
        config.setBasePath("http://" + getNonLoopbackIPv4AddressForThisHost() + ":" + httpConfiguration.getPort());

        environment.addFilter(CrossOriginFilter.class, "/*");
    }

    private String getNonLoopbackIPv4AddressForThisHost() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (!networkInterface.isLoopback()) {
                return getIPv4InetAddressFrom(networkInterface);
            }
        }
        throw new RuntimeException("Can't find a non-loopback IP address");
    }

    private String getIPv4InetAddressFrom(NetworkInterface networkInterface) {
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
            String hostAddress = inetAddresses.nextElement().getHostAddress();
            if (hostAddress.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                return hostAddress;
            }
        }
        throw new RuntimeException("No IPv4 Address");
    }
}
