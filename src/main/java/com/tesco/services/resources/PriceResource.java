package com.tesco.services.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.services.core.Product;
import com.tesco.services.resources.model.ProductPriceBuilder;
import com.tesco.services.core.Store;
import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.tesco.services.repositories.ProductRepository;
import com.tesco.services.repositories.StoreRepository;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.yammer.metrics.annotation.ExceptionMetered;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

import static com.tesco.services.resources.HTTPResponses.badRequest;
import static com.tesco.services.resources.HTTPResponses.notFound;
import static com.tesco.services.resources.HTTPResponses.ok;
import static org.apache.commons.lang.StringUtils.isBlank;

@Path("/price")
@Api(value = "/price", description = "Price API")
@Produces(ResourceResponse.RESPONSE_TYPE)
public class PriceResource {

    public static final int NATIONAL_PRICE_ZONE_ID = 1;
    public static final String NATIONAL_ZONE_CURRENCY = "GBP";
    private static final int NATIONAL_PROMO_ZONE_ID = 5;

    public static final String STORE_NOT_FOUND = "Store not found";
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String PRODUCT_OR_STORE_NOT_FOUND = PRODUCT_NOT_FOUND + " / " + STORE_NOT_FOUND;


    private CouchbaseConnectionManager couchbaseConnectionManager;
    private CouchbaseWrapper couchbaseWrapper;
    private ObjectMapper mapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;

    public PriceResource(CouchbaseConnectionManager couchbaseConnectionManager) {
        this.couchbaseConnectionManager = couchbaseConnectionManager;
    }
    public PriceResource(CouchbaseWrapper couchbaseWrapper,AsyncCouchbaseWrapper asyncCouchbaseWrapper,ObjectMapper mapper) {
        this.couchbaseWrapper = couchbaseWrapper;
        this.asyncCouchbaseWrapper = asyncCouchbaseWrapper;
        this.mapper = mapper;
    }

    @GET
    @Path("/{tpnIdentifier}/{tpn}")
    @ApiOperation(value = "Find price of product variants by product's base TPNB or variants' TPNC")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message =  PRODUCT_OR_STORE_NOT_FOUND),
            @ApiResponse(code = 400, message = HTTPResponses.INVALID_REQUEST),
            @ApiResponse(code = 500, message = HTTPResponses.INTERNAL_SERVER_ERROR)
    })
    public Response get(
            @ApiParam(value = "Type of identifier(B => TPNB, C => TPNC)", required = true) @PathParam("tpnIdentifier") String tpnIdentifier,
            @ApiParam(value = "TPNB/TPNC of Product", required = true) @PathParam("tpn") String tpn,
            @ApiParam(value = "ID of Store if a store-specific price is desired", required = false) @QueryParam("store") String storeId,
            @Context UriInfo uriInfo) throws IOException {

        if (storeQueryParamWasSentWithoutAStoreID(storeId, uriInfo.getQueryParameters())) return badRequest();

/*
        ProductRepository productRepository = new ProductRepository(couchbaseConnectionManager.getCouchbaseClient());
*/

        ProductRepository productRepository = new ProductRepository(couchbaseWrapper,asyncCouchbaseWrapper,mapper);

        Optional<Product> productContainer ;
        if(tpnIdentifier.equals("C")){
            try {
                int item = Integer.parseInt(tpn);
            }
            catch(NumberFormatException ne)
            {
                return notFound(PRODUCT_NOT_FOUND);
            }
            String tpnb = (String) couchbaseWrapper.get(tpn);
            if(tpnb.length()==12)
                tpnb = productRepository.isSpaceOrNull(tpnb)?"":tpnb.substring(1,10);
            productContainer = productRepository.getByTPNB(tpnb,tpn);

        }
        else
        {
            productContainer = productRepository.getByTPNB(tpn);

        }

        if (!productContainer.isPresent()) return notFound(PRODUCT_NOT_FOUND);

        if (storeId == null) {
            return getPriceResponse(productContainer, Optional.of(NATIONAL_PRICE_ZONE_ID), Optional.of(NATIONAL_PROMO_ZONE_ID), NATIONAL_ZONE_CURRENCY);
        }

        return getPriceResponse(storeId, productContainer);
    }

    private Response getPriceResponse(String storeIdValue, Optional<Product> productContainer) throws IOException {

        StoreRepository storeRepository = new StoreRepository(couchbaseWrapper,asyncCouchbaseWrapper,mapper);
        int storeId;

        try {
            storeId = Integer.parseInt(storeIdValue);
        } catch (NumberFormatException e) {
            return notFound(STORE_NOT_FOUND);
        }

        Optional<Store> storeContainer = storeRepository.getByStoreId(String.valueOf(storeId));

        if (!storeContainer.isPresent()) return notFound(STORE_NOT_FOUND);

        Store store = storeContainer.get();
        return getPriceResponse(productContainer, store.getPriceZoneId(), store.getPromoZoneId(), store.getCurrency());
    }

    private Response getPriceResponse(Optional<Product> productContainer, Optional<Integer> priceZoneId, Optional<Integer> promoZoneId, String currency) {
        ProductPriceBuilder productPriceVisitor = new ProductPriceBuilder(priceZoneId, promoZoneId, currency);
        productContainer.get().accept(productPriceVisitor);
        return ok(productPriceVisitor.getPriceInfo());
    }

    @GET
    @Path("/{tpnIdentifier}/{tpn}/{path: .*}")
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
