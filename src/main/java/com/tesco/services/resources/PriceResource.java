package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
public class PriceResource {

    private PriceDAO priceDAO;
    HashMap<String,String> queryParamToMongoKeyMap = new HashMap<String, String>();

    public PriceResource(PriceDAO priceDAO) {
        this.priceDAO = priceDAO;
        queryParamToMongoKeyMap.put("item_number", "itemNumber");
    }

    @GET
    public Response get(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        String key = null;
        String value = null;

        Set<String> keys = queryParameters.keySet();
        for (String queryParamKey : keys){
            if (queryParamToMongoKeyMap.containsKey(queryParamKey.toLowerCase())) {
                key = queryParamToMongoKeyMap.get(queryParamKey.toLowerCase());
                value = queryParameters.getFirst(queryParamKey);
                break;
            }
        }

        if(key == null) return notFound();

        List<DBObject> price = priceDAO.getPriceBy(key, value);
        System.out.println(price);
        return buildResponse(price, Optional.fromNullable(queryParameters.getFirst("callback")));
    }

    private Response buildResponse(List<DBObject> price, Optional<String> callback) {
        if (price.isEmpty())
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
