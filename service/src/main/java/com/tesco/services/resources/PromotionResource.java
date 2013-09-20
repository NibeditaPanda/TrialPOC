package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PromotionDAO;
import com.tesco.services.DAO.Result;
import com.tesco.services.resources.model.Promotion;
import com.tesco.services.resources.model.PromotionRequest;
import com.tesco.services.resources.model.PromotionRequestList;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import static ch.lambdaj.Lambda.*;
import static com.tesco.services.HTTPResponses.*;

@Path("/promotion")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class PromotionResource {

    private PromotionDAO promotionDAO;

    public PromotionResource(PromotionDAO promotionDAO) {

        this.promotionDAO = promotionDAO;
    }

    @GET
    @Path("/{promotionIds}")
    @Metered(name = "getByOfferedId-Meter", group = "PriceServices")
    @Timed(name = "getByOfferedId-Timer", group = "PriceServices")
    @ExceptionMetered(name = "getByOfferedId-Failures", group = "PriceServices")
    public Response getByOfferId(@PathParam("promotionIds") String offerIds,
                                 @QueryParam("tpnb") Optional<String> tpnb,
                                 @QueryParam("store") Optional<String> storeId) {
        Result<DBObject> promotions;
        List<String> ids = Arrays.asList(offerIds.split(","));
        if (tpnb.isPresent() && storeId.isPresent()) {
            promotions = promotionDAO.findTheseOffersAndFilterBy(ids, tpnb.get(), storeId.get());
        } else {
            promotions = promotionDAO.findOffersForTheseIds(ids);
        }

        if (promotions.isEmpty()) {
            return notFound("Promotions Not Found");
        }
        return ok(promotions);
    }

    @POST
    @Path("/find")
    @Metered(name = "getByOfferedId-Meter", group = "PriceServices")
    @Timed(name = "getByOfferedId-Timer", group = "PriceServices")
    @ExceptionMetered(name = "getByOfferedId-Failures", group = "PriceServices")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getByOfferId(@Valid PromotionRequestList promotionRequestList) {

        List<String> ids = extract(promotionRequestList.getPromotions(), on(PromotionRequest.class).getOfferId());
        Result<DBObject> promotions = promotionDAO.findOffersForTheseIds(ids);

        Set<PromotionRequest> uniqueRequests = new HashSet<PromotionRequest>(promotionRequestList.getPromotions());

        List<Promotion> mongoResults = new LinkedList<Promotion>();
        for(DBObject obj : promotions.items())
        {
            mongoResults.add(new Promotion(obj.get("offerId").toString(), obj.get("itemNumber").toString(), obj.get("zoneId").toString()));
        }

        Map<Integer, Promotion> promotionsMap = index(mongoResults, on(Promotion.class).hash());
        List<Promotion> results = new LinkedList<Promotion>();

        for(PromotionRequest promRequest : uniqueRequests)
        {
            if(promotionsMap.containsKey(promRequest.hashCode()))
            {
                results.add(promotionsMap.get(promRequest));
            }
        }

        return ok(results);
    }

    @GET
    @Path("/{promotionId}/{path: .*}")
    @ExceptionMetered(name = "getByOffered-Failures", group = "PriceServices")
    public Response getOffer() {
        return badRequest();
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }
}
