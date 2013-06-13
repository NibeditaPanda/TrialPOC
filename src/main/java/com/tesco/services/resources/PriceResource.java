package com.tesco.services.resources;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.services.DAO.PriceDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
public class PriceResource {

    public static final String NATIONAL_ZONE = "5";
    public static final String ZONE_ID = "zoneId";
    public static final String ZONES = "zones";
    public static final String DEFAULT_CURRENCY = "GBP";
    public static final String PRICE = "price";
    public static final String PROMO_PRICE = "promoPrice";
    public static final String CURRENCY = "currency";

    private PriceDAO priceDAO;

    public PriceResource(PriceDAO priceDAO) {
        this.priceDAO = priceDAO;
    }

    @GET
    public Response get(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        String itemNumber = queryParameters.getFirst("item_number");
        String storeId = queryParameters.getFirst("store");
        if (itemNumber == null) return notFound();

        Optional<DBObject> prices = getPricesFor(itemNumber, storeId);
        if (!prices.isPresent()) return notFound();

        return buildResponse(prices.get(), Optional.fromNullable(queryParameters.getFirst("callback")));
    }

    private Optional<DBObject> getPricesFor(String itemNumber, String storeId) {
        if (storeId != null) return Optional.fromNullable(getPriceByStore(itemNumber, storeId));
        return getNationalPrice(itemNumber);
    }

    private DBObject getPriceByStore(String itemNumber, String storeId) {

        Optional<DBObject> item = priceDAO.getPrice(itemNumber);
        Optional<DBObject> store = priceDAO.getStore(storeId);

        if (!item.isPresent() || !store.isPresent()) return null;

        String zoneId = store.get().get(ZONE_ID).toString();
        String currency = store.get().get(CURRENCY).toString();

        DBObject zones = (DBObject) item.get().get(ZONES);
        DBObject zone = (DBObject) zones.get(zoneId);
        String price = zone.get(PRICE).toString();
        String promoPrice = zone.get(PROMO_PRICE).toString();

        return buildPriceResponse(itemNumber, price, promoPrice, currency);
    }

    private Optional<DBObject> getNationalPrice(String itemNumber) {
        Optional<DBObject> item = priceDAO.getPrice(itemNumber);
        if (item.isPresent()) {
            String price = ((DBObject) ((DBObject) item.get().get(ZONES)).get(NATIONAL_ZONE)).get(PRICE).toString();
            String promoPrice = ((DBObject) ((DBObject) item.get().get(ZONES)).get(NATIONAL_ZONE)).get(PROMO_PRICE).toString();
            return Optional.fromNullable(buildPriceResponse(itemNumber, price, promoPrice, DEFAULT_CURRENCY));
        }
        return item;
    }
    public DBObject buildPriceResponse(String itemNumber, String price, String promoPrice, String currency) {
        BasicDBObject responseObject = new BasicDBObject();
        responseObject.put(PriceDAO.ITEM_NUMBER, itemNumber);
        responseObject.put(PRICE, price);
        responseObject.put(PROMO_PRICE, promoPrice);
        responseObject.put(CURRENCY, currency);
        return responseObject;
    }



    private Response buildResponse(DBObject price, Optional<String> callback) {
        if (callback.isPresent())
            return ok(callback.get() + "(" + price + ")");
        return ok(price);
    }

    private Response ok(Object entity) {
        return Response.status(200).entity(entity).build();
    }

    private Response notFound() {
        return Response.status(404).build();
    }
}
