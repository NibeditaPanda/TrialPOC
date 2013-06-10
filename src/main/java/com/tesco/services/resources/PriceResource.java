package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
public class PriceResource {

    private PriceDAO priceDAO;

    public PriceResource(PriceDAO priceDAO) {
        this.priceDAO = priceDAO;
    }

    @GET
    public Response get(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        String itemNumber = queryParameters.getFirst("item_number");
        if (itemNumber == null) return notFound();
        List<DBObject> result = priceDAO.getPriceBy("itemNumber", itemNumber);
        if (result.isEmpty()) return notFound();
        return buildResponse(result.get(0), Optional.fromNullable(queryParameters.getFirst("callback")));
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
