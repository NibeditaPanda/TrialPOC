package com.tesco.services.resources;

import com.sun.jersey.core.spi.factory.ResponseImpl;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.services.Configuration;
import com.tesco.services.adapters.core.ImportJob;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.ProductRepository;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ok;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
//@Produces(ResourceResponse.RESPONSE_TYPE)
public class ImportResource {
    /*Added by Sushil - PS-83 added logger to log exceptions -Start*/
    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private Configuration configuration;
    private CouchbaseConnectionManager couchbaseConnectionManager;
    private CouchbaseWrapper couchbaseWrapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;

    public ImportResource(Configuration configuration, CouchbaseConnectionManager couchbaseConnectionManager) {
        this.configuration = configuration;
        this.couchbaseConnectionManager = couchbaseConnectionManager;
    }
    public ImportResource(Configuration configuration, CouchbaseWrapper couchbaseWrapper,AsyncCouchbaseWrapper asyncCouchbaseWrapper) {
        this.configuration = configuration;
        this.couchbaseWrapper = couchbaseWrapper;
        this.asyncCouchbaseWrapper = asyncCouchbaseWrapper;
    }

    @POST
    @Path("/import")
    @Metered(name = "postImport-Meter", group = "PriceServices")
    @Timed(name = "postImport-Timer", group = "PriceServices")
    @ExceptionMetered(name = "postImport-Failures", group = "PriceServices")
    public Response importData() {
        try {
            final ImportJob importJob = new ImportJob(configuration.getRPMStoreDataPath(),
                    configuration.getSonettoPromotionsXMLDataPath(),
                    configuration.getSonettoPromotionXSDDataPath(),
                    configuration.getSonettoShelfImageUrl(),
                    configuration.getRPMPriceZoneDataPath(),
                    configuration.getRPMPromoZoneDataPath(),
                    configuration.getRPMPromoExtractDataPath(),
                    configuration.getRPMPromoDescExtractDataPath(),
                    couchbaseWrapper,
                    asyncCouchbaseWrapper);
            Thread thread = new Thread(importJob);
            /*Added by Sushil PS-114 to initialize configuration information for products- Start*/
            ProductRepository productRepository  = new ProductRepository(configuration);
            /*Added by Sushil PS-114 to initialize configuration information for products- End*/
            thread.start();
        } catch (ConfigurationException e) {
            logger.info("error : Import Failed - "+((ResponseImpl)Response.serverError().build()).getStatusType().getStatusCode()+"-{"
                    +((ResponseImpl)Response.serverError().build()).getStatusType().getReasonPhrase()+"}");
            Response.serverError();
        }
        /*Added by Sushil - PS-83 added logger to log exceptions -End*/
        return ok("{\"message\":\"Import Started.\"}").build();
    }
}
