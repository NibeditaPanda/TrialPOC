package com.tesco.services.resources;

import com.tesco.services.DAO.PromotionDAO;
import com.tesco.services.resources.model.Promotion;
import com.tesco.services.resources.model.PromotionRequest;
import com.tesco.services.resources.model.PromotionRequestList;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
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
@Api(value = "/promotion", description = "Promotion API")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class PromotionResource {

    private PromotionDAO promotionDAO;

    public PromotionResource(PromotionDAO promotionDAO) {
        this.promotionDAO = promotionDAO;
    }

    @POST
    @Path("/find")
    @ApiOperation(value = "Find promotional prices by OfferID - ZoneId - ItemNumber (product's base tPNB)")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Error processing your request")})
    @Metered(name = "getByOfferedId-Meter", group = "PriceServices")
    @Timed(name = "getByOfferedId-Timer", group = "PriceServices")
    @ExceptionMetered(name = "getByOfferedId-Failures", group = "PriceServices")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getByOfferId(@Valid PromotionRequestList promotionRequestList) {

        Set<PromotionRequest> uniqueRequests = new HashSet<>(promotionRequestList.getPromotions());
        List<String> ids = extract(uniqueRequests, on(PromotionRequest.class).getOfferId());
        List<Promotion> promotions = promotionDAO.findOffers(ids);

        Map<Integer, Promotion> promotionsMap = index(promotions, on(Promotion.class).hash());
        List<Promotion> results = new LinkedList<>();

        for(PromotionRequest promotionRequest: uniqueRequests)
        {
            if(promotionsMap.containsKey(promotionRequest.hashCode()))
            {
                results.add(promotionsMap.get(promotionRequest.hashCode()));
            }
        }

        return ok(results);
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }
}
