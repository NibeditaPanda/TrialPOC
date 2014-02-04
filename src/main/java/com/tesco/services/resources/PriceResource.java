package com.tesco.services.resources;

import com.mongodb.DBObject;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductPriceBuilder;
import com.tesco.services.dao.PriceDAO;
import com.tesco.services.exceptions.ItemNotFoundException;
import com.tesco.services.repositories.DataGridResource;
import com.tesco.services.repositories.ProductPriceRepository;
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
import java.util.Map;

import static com.tesco.services.resources.HTTPResponses.*;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Path("/price")
@Api(value = "/price", description = "Price API")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class PriceResource {

    public static final int NATIONAL_PRICE_ZONE_ID = 1;
    private PriceDAO priceDAO;
    private DataGridResource dataGridResource;

    public PriceResource(PriceDAO priceDAO, DataGridResource dataGridResource) {
        this.priceDAO = priceDAO;
        this.dataGridResource = dataGridResource;
    }

    @GET
    @Path("/{itemNumber}")
    @ApiOperation(value = "Find price by product's base tPNB")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Product not found")})
    @Timed(name = "getPriceItemNumber-Timer", group = "PriceServices")
    @Metered(name = "getPriceItemNumber-Meter", group = "PriceServices")
    @ExceptionMetered(name = "getPriceItemNumber-Failures", group = "PriceServices")
    public Response get(
            @ApiParam(value = "ItemNumber (Base tPNB) of product whose price needs to be fetched", required = true) @PathParam("itemNumber") String itemNumber,
            @ApiParam(value = "ID of Store if a store-specific price is desired", required = false) @QueryParam("store") String storeId,
            @Context UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if (storeQueryParamWasSentWithoutAStoreID(storeId, queryParameters)) return badRequest();

        List<String> itemIds = Arrays.asList(itemNumber.split(","));

        try {
            List<DBObject> prices = isNotBlank(storeId)
                    ? priceDAO.getPriceAndStoreInfo(itemIds, storeId)
                    : priceDAO.getPricesInfo(itemIds);

            return ok(prices);
        } catch (ItemNotFoundException exception) {
            return notFound(exception.getMessage());
        }
    }

    @GET
    @Path("/{tpnIdentifier}/{tpn}")
    public Response get(
            @PathParam("tpnIdentifier") String tpnIdentifier,
            @PathParam("tpn") String tpn
    ) {
        return ok(getProductPrice(tpn));
    }

    private Map<String, Object> getProductPrice(String tpn) {
        ProductPriceRepository productPriceRepository = new ProductPriceRepository(dataGridResource.getProductPriceCache());
        Product product = productPriceRepository.getByTPNB(tpn);
        ProductPriceBuilder productPriceVisitor = new ProductPriceBuilder(NATIONAL_PRICE_ZONE_ID);
        product.accept(productPriceVisitor);
        return productPriceVisitor.getPriceInfo();
    }

    @GET
    @Path("/{itemNumber}/{tpn}/{path: .*}")
    @ExceptionMetered(name = "getPriceItemNumber-Failures", group = "PriceServices")
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
