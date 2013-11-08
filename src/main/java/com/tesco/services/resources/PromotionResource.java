package com.tesco.services.resources;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tesco.services.Promotion;
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
import static com.google.common.collect.Lists.newArrayList;
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
        List<PromotionRequest> uniquePromotionRequest = newArrayList(uniqueRequests);

        List<Promotion> promotions = Lists.transform(uniquePromotionRequest, new Function<PromotionRequest, Promotion>() {
            @Nullable
            @Override
            public Promotion apply(@Nullable PromotionRequest o) {
                List<com.tesco.services.Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber(o.getOfferId(), o.getItemNumber(), o.getZoneId());
                return getFirst(promotions, null);

            }
        });

        Map<Integer, Promotion> promotionsMap = index(promotions, on(Promotion.class).hash());
        List<Promotion> results = new LinkedList<>();

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
