package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.Exceptions.ItemNotFoundException;
import com.tesco.services.processor.PriceProcessor;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static com.tesco.services.HTTPResponses.*;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
public class PriceResource {

    private PriceProcessor priceProcessor;

    public PriceResource(PriceDAO priceDAO) {
        this.priceProcessor = new PriceProcessor(priceDAO);
    }

    @GET
    @Path("/{itemNumber}")
    public Response get(@PathParam("itemNumber") String itemNumber,
                        @QueryParam("store") Optional<String> storeId,
                        @Context UriInfo uriInfo,
                        @QueryParam("callback") Optional<String> callback) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        queryParameters.remove("callback");
        if ((queryParameters.size() > 0) && !storeId.isPresent()) return badRequest();

        DBObject prices;
        try {
            prices = priceProcessor.getPricesFor(itemNumber, storeId);
        } catch (ItemNotFoundException exception) {
            return notFound(exception.getMessage());
        }
        return buildResponse(prices, callback);
    }

    @GET
    @Path("/{itemNumber}/{path: .*}")
    public Response get() {
        return badRequest();
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }

    private Response buildResponse(DBObject price, Optional<String> callback) {
        if (callback.isPresent())
            return ok(callback.get() + "(" + price + ")");
        return ok(price);
    }
}
