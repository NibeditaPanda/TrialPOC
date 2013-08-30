package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.Exceptions.ItemNotFoundException;

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
//
//    @GET
//    @Path("{queryType}")
//    public Response get() {
//        return badRequest();
//    }

    @GET
    @Path("/{itemNumber}/{path: .*}")
    public Response getItem() {
        return badRequest();
    }
//
//    @GET
//    @Path("offer/{typeId}/{path: .*}")
//    public Response getOffer() {
//        return badRequest();
//    }
//
    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }

}
