package com.tesco.services.resources;

import com.mongodb.DBCollection;
import com.tesco.adapters.core.Controller;
import com.tesco.core.Configuration;
import com.tesco.core.DBFactory;
import com.tesco.core.DataGridResource;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import org.apache.commons.configuration.ConfigurationException;
import org.infinispan.Cache;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tesco.core.PriceKeys.*;
import static javax.ws.rs.core.Response.ok;

@Path("/admin")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class ImportResource {
    private Configuration configuration;
    private DataGridResource dataGridResource;

    public ImportResource(Configuration configuration, DataGridResource dataGridResource) {
        this.configuration = configuration;
        this.dataGridResource = dataGridResource;
    }

    @POST
    @Path("/import")
    @Metered(name = "postImport-Meter", group = "PriceServices")
    @Timed(name = "postImport-Timer", group = "PriceServices")
    @ExceptionMetered(name = "postImport-Failures", group = "PriceServices")
    public Response importData() {
        try {
            DBFactory dbFactory = new DBFactory(configuration);

            //TODO Vyv is this a memory leak?
            Cache<String,Object> promotionCache = dataGridResource.getPromotionCache();
            Controller controller = new Controller(
                    configuration.getRPMPriceDataPath(), configuration.getRPMStoreDataPath(),
                    configuration.getRPMPromotionDataPath(),
                    configuration.getSonettoPromotionsXMLDataPath(),
                    configuration.getRPMPromotionDescCSVUrl(),
                    configuration.getSonettoPromotionXSDDataPath(),
                    configuration.getSonettoShelfImageUrl(),
                    promotionCache);

            Thread thread = new Thread(new DataImportRunner(controller, dbFactory));
            thread.start();
        } catch (ConfigurationException e) {
            Response.serverError();
        }

        return ok("{\"message\":\"Import Started.\"}").build();
    }


    private class DataImportRunner implements Runnable {
        private Controller controller;
        private DBFactory dbFactory;

        public DataImportRunner(Controller controller, DBFactory dbFactory) {
            this.controller = controller;
            this.dbFactory = dbFactory;
        }

        @Override
        public void run() {
            DBCollection tempPriceCollection = dbFactory.getCollection(getTempCollectionName(PRICE_COLLECTION));
            DBCollection tempStoreCollection = dbFactory.getCollection(getTempCollectionName(STORE_COLLECTION));
            DBCollection tempPromotionCollection = dbFactory.getCollection(getTempCollectionName(PROMOTION_COLLECTION));

            controller.processData(tempPriceCollection, tempStoreCollection, tempPromotionCollection, true);
        }

        private String getTempCollectionName(String baseCollectionName) {
            return String.format("%s%s", baseCollectionName, new SimpleDateFormat("yyyyMMdd").format(new Date()));
        }

    }
}
