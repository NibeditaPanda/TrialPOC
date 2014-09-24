package com.tesco.services.resources;

import com.sun.jersey.core.spi.factory.ResponseImpl;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.services.Configuration;
import com.tesco.services.adapters.core.ImportJob;
import com.tesco.services.exceptions.ImportInProgressException;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.ProductRepository;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.concurrent.Semaphore;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ok;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
//@Produces(ResourceResponse.RESPONSE_TYPE)
public class ImportResource {
    /*Added by Sushil - PS-83 added logger to log exceptions -Start*/
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    /*Added by Salman - PS-242 added semaphore -Start*/
    public static Semaphore importSemaphore = new Semaphore(1);

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
        /** Added by Salman - PS-242 added new condition to prevent importing data when one
         import is already in progress **/
        if (!importSemaphore.tryAcquire()) {
            logger.info("Import already running");
            throw new ImportInProgressException();
        }
        try {
            ImportJob.errorString=null;
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
            thread.start();
        } catch (ConfigurationException e) {
            logger.info("error : Import Failed - "+((ResponseImpl)Response.serverError().build()).getStatusType().getStatusCode()+"-{"
                    +((ResponseImpl)Response.serverError().build()).getStatusType().getReasonPhrase()+"}");
            Response.serverError();
        }
        /*Added by Sushil - PS-83 added logger to log exceptions -End*/
        return ok("{\"message\":\"Import Started.\"}").build();
    }

    /** Added by Salman - PS-242 added new GET call to check if import is completed or errored out */
    @GET
    @Path("/importInProgress")
    public Response isImportInProgress() {
        String val = importSemaphore.availablePermits() >= 1 ? "false" : "true";

        if(importSemaphore.availablePermits() <1){
            return Response.ok("{\"import\":\"progress\"}").build();
        }else if(ImportJob.errorString!=null){
            System.out.println(ImportJob.errorString);
            return Response.ok(String.format("{\"import\":\"aborted\",\n \"error\":\"%s\"}",ImportJob.errorString)).build();
        }else{
            return Response.ok("{\"import\":\"completed\"}").build();
        }
    }
}
