package com.tesco.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.ConcreteCouchbaseResource;
import com.tesco.couchbase.CouchbaseResource;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.services.adapters.core.ImportJob;
import com.tesco.services.adapters.rpm.writers.ProductMapper;
import com.tesco.services.adapters.rpm.writers.RPMWriter;
import com.tesco.services.adapters.rpm.writers.StoreMapper;
import com.tesco.services.healthChecks.ServiceHealthCheck;
import com.tesco.services.mappers.InvalidUrlMapper;
import com.tesco.services.mappers.ServerErrorMapper;
import com.tesco.services.metrics.ResourceMetricsListener;
import com.tesco.services.repositories.*;
import com.tesco.services.resources.ImportResource;
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
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.FactoryInjector;

import java.lang.management.MemoryUsage;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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
        MutablePicoContainer container = configureDependencies(configuration);

        final CouchbaseConnectionManager couchbaseConnectionManager = new CouchbaseConnectionManager(configuration);
        /*
        environment.addResource(new PriceResource(couchbaseConnectionManager));
        final PromotionRepository promotionRepository = new PromotionRepository(new UUIDGenerator(), null);
        environment.addResource(new PromotionResource(promotionRepository));
        environment.addResource(new VersionResource());
        environment.addResource(new ImportResource(configuration, couchbaseConnectionManager));*/

        registerResources(environment, container);
        environment.addProvider(new InvalidUrlMapper());
        environment.addProvider(new ServerErrorMapper());

        /**
         *   @Toy commented out Graphite because tesco boxes don't have access to the Internet at the moment
         */
//        configureMetrics(configuration);

        environment.addHealthCheck(new ServiceHealthCheck(couchbaseConnectionManager));
       // environment.addHealthCheck(new CouchbaseHealthCheck(container.getComponent(AsyncCouchbaseWrapper.class)));

        configureSwagger(environment, configuration);
    }

    private void registerResources(Environment environment, MutablePicoContainer container) {
        HashMap<Class, FactoryInjector> resourceAdaptors = new HashMap<>();
        resourceAdaptors.put(ImportResource.class, importResourceInjector());
        resourceAdaptors.put(PriceResource.class, priceResourceInjector());
        resourceAdaptors.put(PromotionResource.class, promotionResourceInjector());


        HashSet<Class> resourceList = new HashSet<>();
        resourceList.add(ImportResource.class);
        resourceList.add(PriceResource.class);
        resourceList.add(PromotionResource.class);

        // Lets actually add the resources. We have to do it in two places.
        // 1 is in the environment to let jersey know about them
        // 2 is in pico to let pico know it has to resolve dependencies for them
        for (Class resource : resourceList) {
            if(resourceAdaptors.containsKey(resource)){
                container.addAdapter(resourceAdaptors.get(resource));
            } else {
                container.addComponent(resource);
            }
            environment.addResource(resource);
        }

        // And now for the dangerous singletons ;)
       // environment.addResource(new MemoryUsage());
        environment.addResource(new VersionResource());

        // Add Pico as a provider for dependencies to the environment
        environment.addProvider(new PicoProvider(container, resourceList));
    }
    private FactoryInjector<ImportResource> importResourceInjector() {
        return new FactoryInjector<ImportResource>() {
            @Override
            public ImportResource getComponentInstance(PicoContainer picoContainer, Type type) {
                CouchbaseWrapper couchbaseWrapper = picoContainer.getComponent(CouchbaseWrapper.class);
                AsyncCouchbaseWrapper asyncCouchbaseWrapper = picoContainer.getComponent(AsyncCouchbaseWrapper.class);
                return new ImportResource(picoContainer.getComponent(Configuration.class),couchbaseWrapper,asyncCouchbaseWrapper);
            }
        };
    }

    private FactoryInjector<PriceResource> priceResourceInjector() {
        return new FactoryInjector<PriceResource>() {
            @Override
            public PriceResource getComponentInstance(PicoContainer picoContainer, Type type) {
                CouchbaseWrapper couchbaseWrapper = picoContainer.getComponent(CouchbaseWrapper.class);
                AsyncCouchbaseWrapper asyncCouchbaseWrapper = picoContainer.getComponent(AsyncCouchbaseWrapper.class);
                ObjectMapper mapper = picoContainer.getComponent(ObjectMapper.class);
                return new PriceResource(couchbaseWrapper,asyncCouchbaseWrapper,mapper);
            }
        };
    }
    private FactoryInjector<PromotionResource> promotionResourceInjector() {
        return new FactoryInjector<PromotionResource>() {
            @Override
            public PromotionResource getComponentInstance(PicoContainer picoContainer, Type type) {
                //CouchbaseWrapper couchbaseWrapper = picoContainer.getComponent(CouchbaseWrapper.class);
                //ObjectMapper mapper = picoContainer.getComponent(ObjectMapper.class);
                PromotionRepository promotionRepository = picoContainer.getComponent(PromotionRepository.class);
                return new PromotionResource(promotionRepository);
            }
        };
    }
    private MutablePicoContainer configureDependencies(Configuration configuration) {
        MutablePicoContainer container = new DefaultPicoContainer();
        container.addComponent(configuration);

        // CouchBase
        String[] couchbaseNodes = configuration.getCouchbaseNodes();//currently only one node for Price Services
        String couchbaseBucket = configuration.getCouchbaseBucket();
        String couchbaseUsername = configuration.getCouchbaseUsername();
        String couchbasePassword = configuration.getCouchbasePassword();

        CouchbaseResource couchbaseResource = new ConcreteCouchbaseResource(couchbaseNodes, couchbaseBucket, couchbaseUsername, couchbasePassword);
        container.addComponent(couchbaseResource.getCouchbaseWrapper());
        container.addComponent(couchbaseResource.getAsyncCouchbaseWrapper());
       // container.addComponent(new ProductKeyGenerator());

        // Mapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new AfterburnerModule());
        container.addComponent(objectMapper);

        // Repos
        container.addComponent(ProductRepository.class);
        container.addComponent(StoreRepository.class);
        container.addComponent(PromotionRepository.class);

        //Async Repos
        container.addComponent(AsyncReadWriteProductRepository.class);
       // container.addComponent(AsyncReadWriteStoreRepository.class);

        //container.addComponent(AsyncIndexRepository.class);
        //container.addComponent(AsyncCommercialHierarchyRepository.class);
        //container.addComponent(ProductViewRepository.class);

        // Sonetto Adapter yet to be implemented
        //container.addComponent(SonettoProductsUpdater.class);
        //container.addComponent(SonettoProductsExtraInfoUpdater.class);
        //container.addComponent(SonettoController.class);
        //container.addComponent(SonettoProductExtraInfoTransformer.class);

        // RPM Adapter
        container.addComponent(ImportJob.class);
        container.addComponent(RPMWriter.class);
        container.addComponent(ProductMapper.class);
        container.addComponent(StoreMapper.class);

        return container;
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
        config.setBasePath("http://" + getNonLoopbackIPv4AddressForThisHost() + ":" + httpConfiguration.getPort());

        environment.addFilter(CrossOriginFilter.class, "/*");
    }

    private String getNonLoopbackIPv4AddressForThisHost() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                return getIPv4InetAddressFrom(networkInterface);
            }
        }
        //throw new RuntimeException("Can't find a non-loopback IP address");
        return "localhost";
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
