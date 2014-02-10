package com.tesco.services.resources;

import com.mongodb.DBObject;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductPriceBuilder;
import com.tesco.services.core.Store;
import com.tesco.services.dao.PriceDAO;
import com.tesco.services.exceptions.ItemNotFoundException;
import com.tesco.services.repositories.DataGridResource;
import com.tesco.services.repositories.ProductPriceRepository;
import com.tesco.services.repositories.StoreRepository;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;

import static com.tesco.services.resources.HTTPResponses.badRequest;
import static com.tesco.services.resources.HTTPResponses.notFound;
import static com.tesco.services.resources.HTTPResponses.ok;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Path("/price")
@Api(value = "/price", description = "Price API")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class PriceResource {

    public static final int NATIONAL_PRICE_ZONE_ID = 1;
    public static final String NATIONAL_ZONE_CURRENCY = "GBP";
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
    @ApiOperation(value = "Find price of product variants by product's base TPNB or variants' TPNC")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Product not found")})
    public Response get(
            @ApiParam(value = "Type of identifier(B => TPNB, C => TPNC)", required = true) @PathParam("tpnIdentifier") String tpnIdentifier,
            @ApiParam(value = "TPNB/TPNC of Product", required = true) @PathParam("tpn") String tpn,
            @ApiParam(value = "ID of Store if a store-specific price is desired", required = false) @QueryParam("store") Integer storeId) {

        ProductPriceRepository productPriceRepository = new ProductPriceRepository(dataGridResource.getProductPriceCache());
        Product product = productPriceRepository.getByTPNB(tpn);

        ProductPriceBuilder productPriceVisitor = getProductPriceBuilder(storeId);
        product.accept(productPriceVisitor);

        return ok(productPriceVisitor.getPriceInfo());
    }

    private ProductPriceBuilder getProductPriceBuilder(Integer storeId) {
        if (storeId == null) {
            return new ProductPriceBuilder(NATIONAL_PRICE_ZONE_ID, NATIONAL_ZONE_CURRENCY);
        }

        StoreRepository storeRepository = new StoreRepository(dataGridResource.getStoreCache());
        final Store store = storeRepository.getByStoreId(storeId);

        return new ProductPriceBuilder(store.getPriceZoneId().get(), store.getCurrency());
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
