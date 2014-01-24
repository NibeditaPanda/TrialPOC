package com.tesco.services.resources;

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

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.tesco.services.resources.HTTPResponses.badRequest;
import static com.tesco.services.resources.HTTPResponses.ok;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
    @Consumes(APPLICATION_JSON)
    public Response getByOfferId(@Valid PromotionRequestList promotionRequestList) {

        Set<PromotionRequest> uniqueRequests = new HashSet<>(promotionRequestList.getPromotions());
        List<PromotionRequest> uniquePromotionRequest = newArrayList(uniqueRequests);

        Set<Promotion> results = new HashSet<>();

        for( PromotionRequest promotionRequest : uniquePromotionRequest) {
            List<Promotion> promotions = promotionRepository.getPromotionsByOfferIdZoneIdAndItemNumber(promotionRequest.getOfferId(),
                    promotionRequest.getItemNumber(),
                    promotionRequest.getZoneId());

            List<Promotion> nonNullPromotions = newArrayList(filter(promotions, notNull()));
            results.addAll(nonNullPromotions);
        }

        return ok(results);
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }
}
