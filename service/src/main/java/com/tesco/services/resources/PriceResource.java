package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.Exceptions.ItemNotFoundException;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static com.tesco.services.HTTPResponses.*;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
public class PriceResource {

    private PriceDAO priceDAO;

    public PriceResource(PriceDAO priceDAO) {
        this.priceDAO = priceDAO;
    }

    @GET
    @Path("/{itemNumber}")
    @Metered(name="getPriceItemNumber-Meter",group="PriceServices")
    @Timed(name="getPriceItemNumber-Timer",group="PriceServices")
    @ExceptionMetered(name="getPriceItemNumber-Failures",group="PriceServices")
    public Response get(@PathParam("itemNumber") String itemNumber,
                        @QueryParam("store") Optional<String> storeId,
                        @Context UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if ((queryParameters.size() > 0) && !storeId.isPresent()) return badRequest();

        DBObject prices;
        try {
            prices = storeId.isPresent()

                    ? priceDAO.getPriceAndStoreInfo(itemNumber,storeId.get())
                    : priceDAO.getPricesInfo(itemNumber);

        } catch (ItemNotFoundException exception) {
            return notFound(exception.getMessage());
        }
        return ok(prices);
    }

    @GET
    @Path("/{itemNumber}/{path: .*}")
    @ExceptionMetered(name="getPriceItemNumber-Failures",group="PriceServices")
    public Response getItem() {
        return badRequest();
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }

}
