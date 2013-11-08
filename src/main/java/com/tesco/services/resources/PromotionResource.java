package com.tesco.services.resources;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tesco.services.repositories.PromotionRepository;
import com.tesco.services.resources.model.PromotionRequest;
import com.tesco.services.resources.model.PromotionRequestList;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import static ch.lambdaj.Lambda.*;
import static com.google.common.collect.Iterables.getFirst;
import static com.tesco.services.HTTPResponses.badRequest;
import static com.tesco.services.HTTPResponses.ok;

@Path("/promotion")
@Api(value = "/promotion", description = "Promotional Price Endpoints")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class PromotionResource {

    private PromotionRepository promotionRepository;

    public PromotionResource(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
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

        List<com.tesco.services.Promotion> promotions = Lists.transform(ids, new Function<String, com.tesco.services.Promotion>() {
            @Nullable
            @Override
            public com.tesco.services.Promotion apply(@Nullable String id) {
                List<com.tesco.services.Promotion> promotions = promotionRepository.getPromotionsByOfferId(id);
                return getFirst(promotions, null);

            }


        });


        Map<Integer, com.tesco.services.Promotion> promotionsMap = index(promotions, on(com.tesco.services.Promotion.class).hash());
        List<com.tesco.services.Promotion> results = new LinkedList<>();

        for (PromotionRequest promotionRequest : uniqueRequests) {
            if (promotionsMap.containsKey(promotionRequest.hashCode())) {
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
