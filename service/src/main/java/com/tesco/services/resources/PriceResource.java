package com.tesco.services.resources;

import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;
import com.tesco.services.Exceptions.ItemNotFoundException;
import com.wordnik.swagger.annotations.*;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;

import static com.tesco.services.HTTPResponses.*;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Path("/price")
@Api(value = "/price", description = "Price API")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class PriceResource {

    private PriceDAO priceDAO;

    public PriceResource(PriceDAO priceDAO) {
        this.priceDAO = priceDAO;
    }

    @GET
    @Path("/{itemNumber}")
    @ApiOperation(value = "Find price by product's base tPNB")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Product not found")})
    @Timed(name="getPriceItemNumber-Timer",group="PriceServices")
    @Metered(name="getPriceItemNumber-Meter",group="PriceServices")
    @ExceptionMetered(name="getPriceItemNumber-Failures",group="PriceServices")
    public Response get(
      @ApiParam(value = "ItemNumber (Base tPNB) of product whose price needs to be fetched", required = true) @PathParam("itemNumber") String itemNumber,
      @ApiParam(value = "ID of Store if a store-specific price is desired", required = false) @QueryParam("store") String storeId,
      @Context UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if (storeQueryParamWasSentWithoutAStoreID(storeId, queryParameters)) return badRequest();

        List<String> itemIds = Arrays.asList(itemNumber.split(","));

        try {
            List<DBObject> prices = isNotBlank(storeId)
                    ? priceDAO.getPriceAndStoreInfo(itemIds,storeId)
                    : priceDAO.getPricesInfo(itemIds);

            return ok(prices);
        } catch (ItemNotFoundException exception) {
            return notFound(exception.getMessage());
        }
    }

    @GET
    @Path("/{itemNumber}/{path: .*}")
    @ExceptionMetered(name="getPriceItemNumber-Failures",group="PriceServices")
    public Response getItem() {
        return badRequest();
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return badRequest();
    }

    private boolean storeQueryParamWasSentWithoutAStoreID(String storeId, MultivaluedMap<String, String> queryParameters) {
      return (queryParameters.size() > 0) && isBlank(storeId);
    }
}
