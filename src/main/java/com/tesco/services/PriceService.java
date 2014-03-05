package com.tesco.services;

import com.tesco.services.dao.PriceDAO;
import com.tesco.services.healthChecks.ServiceHealthCheck;
import com.tesco.services.mappers.InvalidUrlMapper;
import com.tesco.services.mappers.ServerErrorMapper;
import com.tesco.services.metrics.ResourceMetricsListener;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.repositories.UUIDGenerator;
import com.tesco.services.resources.ImportResource;
import com.tesco.services.resources.MongoUnavailableProvider;
import com.tesco.services.resources.PriceResource;
import com.tesco.services.resources.PromotionResource;
import com.tesco.services.resources.VersionResource;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.HttpConfiguration;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.reporting.GraphiteReporter;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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

        final CouchbaseConnectionManager couchbaseConnectionManager = new CouchbaseConnectionManager(configuration);
        environment.addResource(new PriceResource(new PriceDAO(configuration), couchbaseConnectionManager));
        final PromotionRepository promotionRepository = new PromotionRepository(new UUIDGenerator(), null);
        environment.addResource(new PromotionResource(promotionRepository));
        environment.addResource(new VersionResource());
        environment.addResource(new ImportResource(configuration, couchbaseConnectionManager));

        environment.addProvider(new MongoUnavailableProvider());
        environment.addProvider(new InvalidUrlMapper());
        environment.addProvider(new ServerErrorMapper());

        /**
         *   @Toy commented out Graphite because tesco boxes don't have access to the Internet at the moment
         */
//        configureMetrics(configuration);

        environment.addHealthCheck(new ServiceHealthCheck(configuration));
        configureSwagger(environment, configuration);
    }

    private void configureMetrics(Configuration configuration) {
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
        config.setBasePath("http://" + "localhost" + ":" + httpConfiguration.getPort());

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
