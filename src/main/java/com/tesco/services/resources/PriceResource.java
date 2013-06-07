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
        if(queryParameters.size() == 0) return notFound();

        String value;
        List<DBObject> result = null;

        value = queryParameters.getFirst("zone");
        if(value != null) {
            result = priceDAO.getPriceByZone(value);
        }

        value = queryParameters.getFirst("item_number");
        if(value != null) {
            result = priceDAO.getPriceBy("itemNumber", value);
        }

        value = queryParameters.getFirst("store");
        if(value != null) {
            result = priceDAO.getPriceByStore(value);
        }

        return buildResponse(result, Optional.fromNullable(queryParameters.getFirst("callback")));
    }

    private Response buildResponse(List<DBObject> price, Optional<String> callback) {
        if (price == null || price.isEmpty())
            return notFound();
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
