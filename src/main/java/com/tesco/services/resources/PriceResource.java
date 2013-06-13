package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.processor.PriceProcessor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
public class PriceResource {

    private PriceProcessor priceProcessor;

    public PriceResource(PriceDAO priceDAO) {
        this.priceProcessor = new PriceProcessor(priceDAO);
    }

    @GET
    public Response get(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        String itemNumber = queryParameters.getFirst("item_number");
        String storeId = queryParameters.getFirst("store");
        if (itemNumber == null) return notFound();

        Optional<DBObject> prices = priceProcessor.getPricesFor(itemNumber, storeId);
        if (!prices.isPresent()) return notFound();

        return buildResponse(prices.get(), Optional.fromNullable(queryParameters.getFirst("callback")));
    }

    private Response buildResponse(DBObject price, Optional<String> callback) {
        if (callback.isPresent())
            return ok(callback.get() + "(" + price + ")");
        return ok(price);
    }

    private Response ok(Object entity) {
        return Response.status(200).entity(entity).build();
    }

    private Response notFound() {
        return Response.status(404).build();
    }
}
