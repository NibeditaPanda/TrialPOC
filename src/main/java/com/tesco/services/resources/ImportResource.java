package com.tesco.services.resources;

import com.tesco.services.Configuration;
import com.tesco.services.adapters.core.Product;
import com.tesco.services.dao.DBFactory;
import com.tesco.services.repositories.DataGridResource;
import com.tesco.services.core.Promotion;
import com.tesco.services.adapters.core.ImportJob;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import org.apache.commons.configuration.ConfigurationException;
import org.infinispan.Cache;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
            Cache<String, Promotion> promotionCache = dataGridResource.getPromotionCache();
            Cache<String, Product> productPriceCache = dataGridResource.getProductPriceCache();

            final ImportJob importJob = new ImportJob(configuration.getRPMPriceDataPath(), configuration.getRPMStoreDataPath(),
                    configuration.getRPMPromotionDataPath(),
                    configuration.getSonettoPromotionsXMLDataPath(),
                    configuration.getRPMPromotionDescCSVUrl(),
                    configuration.getSonettoPromotionXSDDataPath(),
                    configuration.getSonettoShelfImageUrl(),
                    configuration.getRPMPriceZoneDataPath(),
                    promotionCache,
                    productPriceCache,
                    dbFactory);

            Thread thread = new Thread(importJob);
            thread.start();
        } catch (ConfigurationException e) {
            Response.serverError();
        }

        return ok("{\"message\":\"Import Started.\"}").build();
    }
}
