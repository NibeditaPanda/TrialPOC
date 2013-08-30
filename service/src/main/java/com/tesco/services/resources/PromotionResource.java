package com.tesco.services.resources;

import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.DAO.PromotionDAO;
import com.tesco.services.Exceptions.ItemNotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @Path("/{promotionId}")
    public Response getByOfferId(@PathParam("promotionId") String offerId) {
        DBObject promotion;
        try {
            promotion = promotionDAO.getOfferBy(offerId);
        } catch (ItemNotFoundException e) {
            return notFound(e.getMessage());
        }
        return ok(promotion);
    }

    @GET
    @Path("/{promotionId}/{path: .*}")
    public Response getOffer() {
        return badRequest();
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }
}
