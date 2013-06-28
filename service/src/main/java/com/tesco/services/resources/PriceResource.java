package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.processor.PriceProcessor;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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

        if((uriInfo.getQueryParameters().size() > 0) && !storeId.isPresent()) return badRequest();
        if (itemNumber == null) return notFound();

        Optional<DBObject> prices = priceProcessor.getPricesFor(itemNumber, storeId);
        if (!prices.isPresent()) return notFound();

        return buildResponse(prices.get(), callback);
    }

    @GET
    @Path("/")
    public Response get(){
        return badRequest();
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

    private Response badRequest() {
        return Response.status(400).build();
    }
}
