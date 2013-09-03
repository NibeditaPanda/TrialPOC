package com.tesco.services.resources;

import com.mongodb.DBObject;
import com.tesco.services.DAO.PromotionDAO;
import com.tesco.services.Exceptions.ItemNotFoundException;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;

import static com.tesco.services.HTTPResponses.badRequest;
import static com.tesco.services.HTTPResponses.notFound;
import static com.tesco.services.HTTPResponses.ok;

@Path("/promotion")
@Produces(MediaType.APPLICATION_JSON)
public class PromotionResource {

    private PromotionDAO promotionDAO;

    public PromotionResource(PromotionDAO promotionDAO) {

        this.promotionDAO = promotionDAO;
    }

    @GET
    @Path("/{promotionIds}")
    @Metered(name="getByOfferedId-Meter",group="PriceServices")
    @Timed(name="getByOfferedId-Timer",group="PriceServices")
    @ExceptionMetered(name="getByOfferedId-Failures",group="PriceServices")
    public Response getByOfferId(@PathParam("promotionIds") String offerIds) {
        List<DBObject> promotion;
        try {
            List<String> ids = Arrays.asList(offerIds.split(","));
            promotion = promotionDAO.findOffersForTheseIds(ids);
        } catch (ItemNotFoundException e) {
            return notFound(e.getMessage());
        }
        return ok(promotion);
    }

    @GET

    @Path("/{promotionId}/{path: .*}")
    @ExceptionMetered(name="getByOffered-Failures",group="PriceServices")
    public Response getOffer() {
        return badRequest();
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }
}
