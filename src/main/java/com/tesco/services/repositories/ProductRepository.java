package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.exceptions.CouchbaseOperationException;
import com.tesco.couchbase.listeners.*;
import com.tesco.services.Configuration;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Store;
import com.tesco.services.exceptions.InvalidDataException;
import com.tesco.services.utility.Dockyard;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class ProductRepository {

    private CouchbaseClient couchbaseClient;
    private ObjectMapper mapper;
    private CouchbaseWrapper couchbaseWrapper;
    private AsyncCouchbaseWrapper asyncCouchbaseWrapper;
    private final Logger logger = getLogger(getClass().getName());

    public Product getProductIdentified() {
        return productIdentified;
    }

    private Product productIdentified;
    public ProductRepository(CouchbaseClient couchbaseClient) {
        this.couchbaseClient = couchbaseClient;
    }
    public ProductRepository(CouchbaseWrapper couchbaseWrapper,AsyncCouchbaseWrapper asyncCouchbaseWrapper,ObjectMapper mapper) {
        this.couchbaseWrapper = couchbaseWrapper;
        this.asyncCouchbaseWrapper = asyncCouchbaseWrapper;
        this.mapper = mapper;

    }

    public Optional<Product> getByTPNB(String tpnb) {
        Product product = new Product();
        String productJson = (String) couchbaseWrapper.get(getProductKey(tpnb));

        if(productJson == null){
            product = null;
        }
        else{
            try {
                product = mapper.readValue(productJson,Product.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (product != null) ? Optional.of(product) : Optional.<Product>absent();
    }

    private String getProductKey(String tpnb) {
        return String.format("PRODUCT_%s", tpnb);
    }

    public void put(Product product) {
        try {
            String productJson = mapper.writeValueAsString(product);
            couchbaseWrapper.set(getProductKey(product.getTPNB()), productJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void getProductByTPNB(String tpnb, final Listener<Product, Exception> listener) {
        final String key = getProductKey(tpnb);
        asyncCouchbaseWrapper.get(key, new GetListener(asyncCouchbaseWrapper, key) {
            @Override
            protected void process(Object jsonProduct) {
                if (jsonProduct != null) {
                    try {
                        Product product = mapper.readValue((String) jsonProduct, Product.class);
                        listener.onComplete(product);
                        productIdentified = product;
                    } catch (IOException e) {
                        throw new InvalidDataException(String.format("Failed to deserialize product json: %s", jsonProduct), e);
                    }
                } else {
                    listener.onComplete(null);
                }
            }

            @Override
            public void onException(Exception e) {
                listener.onException(e);
            }
        });
    }


    public void insertProduct(Product product, final Listener<Void, Exception> listener) {
        String productKey = getProductKey(product.getTPNB());
        logger.debug("({}) insertProduct", product);
        try {
            String jsonProduct = mapper.writeValueAsString(product);
            asyncCouchbaseWrapper.set(productKey, jsonProduct, new SetListener(asyncCouchbaseWrapper, productKey, jsonProduct) {
                @Override
                public void process() {
                    listener.onComplete(null);
                }

                @Override
                public void onException(Exception e) {
                    listener.onException(e);
                }
            });
        } catch (IOException e) {
            listener.onException(e);
        }
    }
    /*Added By Nibedita - PS 78 - Store ITEM and TPNC key value - Start*/
    public void mapTPNC_TPNB(String TPNC , String ITEM){
        try {
            String itemJson = mapper.writeValueAsString(ITEM);
            String tpncJson = mapper.writeValueAsString(TPNC);
            if(Dockyard.isSpaceOrNull(couchbaseWrapper.get(TPNC)))
                couchbaseWrapper.set(TPNC, itemJson);
            if(Dockyard.isSpaceOrNull(couchbaseWrapper.get(ITEM)))
                couchbaseWrapper.set(ITEM, tpncJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public String getProductTPNC(String item) {

        String tpnc = (String)couchbaseWrapper.get(item);
        tpnc = Dockyard.replaceOldValCharWithNewVal(tpnc, "\"", "");
        return tpnc;
    }
    /*Added By Nibedita - PS 78 - Store ITEM and TPNC key value - End*/

    public Optional<Product> getByTPNB(String tpnb,String tpnc) {
        Product product = new Product();
        Product productvar = new Product(tpnb);
        String productJson = (String) couchbaseWrapper.get(getProductKey(tpnb));
        ProductVariant productVariant = new ProductVariant();
        if(productJson == null){
            product = null;
        }
        else{
            try {
                product = mapper.readValue(productJson,Product.class);
                productVariant = product.getProductVariantByTPNC(tpnc);
                productvar.addProductVariant(productVariant);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (productvar != null) ? Optional.of(productvar) : Optional.<Product>absent();
    }

    /*Added By Nibedita - PS 78 - fetch item/tpnc based on tpnc/item input  - Start*/
    /*Modified by Sushil PS-30 Modified method parameter to throw exception -Start */
    public String getMappedTPNCorTPNB(String tpn) throws CouchbaseOperationException {
        String tpnVal = null;
            if(!Dockyard.isSpaceOrNull(tpn)) {
                tpnVal = (String) couchbaseWrapper.get(tpn);
                if(!Dockyard.isSpaceOrNull(tpnVal))
                    tpnVal = tpnVal.replace("\"", "");
            }
        return tpnVal;
    }
    /*Modified by Sushil PS-30 Modified method parameter to throw exception -End */
    /*Added By Nibedita - PS 78 - fetch item/tpnc based on tpnc/item input  - End*/
 /*Added by Sushil PS-114 to get view information from couchbase and process those products which are not update for more than 2 days- start*/

    /**
     * This will get view information from couchbase and process those
     * products which are not update for more than n days
     *
     * @param configuration - Pass configuration values
     * @param couchbaseClient - to get the couch base connection
     * @throws Exception - can throw RuntimeException, InvalidViewException
     */
    public void purgeUnUpdatedItems(CouchbaseClient couchbaseClient, Configuration configuration) throws Exception {
           this.couchbaseClient = couchbaseClient;
           View view = couchbaseClient.getView(configuration.getCouchBaseDesignDocName(), configuration.getCouchBaseViewName());
           runView(view, couchbaseClient, configuration);
    }

    /**
     * This is to query from the view based on query parameters
     *
     * @param view - View parameter is required
     * @param configuration - Pass configuration values
     * @param couchbaseClient - to get the couch base connection
     * @throws Exception - can throw RuntimeException, InvalidViewException
     */
    private void runView(View view, CouchbaseClient couchbaseClient,Configuration configuration) throws Exception{
        String last_update_date_key ="";
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-(configuration.getLastUpdatedPurgeDays()));
        last_update_date_key = dateFormat.format(cal.getTime());
        last_update_date_key =  mapper.writeValueAsString(last_update_date_key);

        Query query = new Query();
        query.setRangeEnd(last_update_date_key);
        query.setStale(Stale.FALSE);

        ViewResponse response = couchbaseClient.query(view, query);
        logger.info("message : Initializing purge operation for Products Last Updated on "+last_update_date_key);
        for(ViewRow row : response) {
            delete_TPNB_TPNC_VAR(row.getId(), couchbaseClient);
        }
    }
    /*Added by Sushil PS-114 to get view information from couchbase and process those products which are not update for more than 2 days- end*/

        /*Added by Salman for PS-114 to delete the results return from the view - Start*/

    /**
     * This will delete the documents matching with product_key from couchbase.
     * @param product_key - This key is JSON document key for couchbase database to get the documents
     * @param couchbaseClient - to get the couch base connection
     */
    public void delete_TPNB_TPNC_VAR(String product_key, CouchbaseClient couchbaseClient){
        Product product= getByTPNBWithCouchBaseClient(product_key.split("_")[1], couchbaseClient).or(new Product());
        Set<String> tpncList=product.getTpncToProductVariant().keySet();

        Iterator tpnciterator =tpncList.iterator();
        while (tpnciterator.hasNext()) {
            String tpnc=tpnciterator.next().toString();
            String tpnborvar=getMappedTPNCorTPNBWithCouchBaseClient(tpnc, couchbaseClient);
            //check if mapping is not present then skip the tpnc mapping documents deletion
            if(!Dockyard.isSpaceOrNull(tpnborvar)) {
                deleteProduct(tpnborvar, couchbaseClient);
                deleteProduct(tpnc, couchbaseClient);
            }
        }
        deleteProduct(product_key, couchbaseClient);

    }

    /**
     * @param product_key - This key is JSON document key for couchbase database to get the documents
     * @param couchbaseClient - to get the couch base connection
     */
    public void deleteProduct(String product_key, CouchbaseClient couchbaseClient){
       final String productKey = product_key;

        couchbaseClient.delete(product_key);
    }
        /*Added by Salman for PS-114 to delete the results return from the view - End*/


        /*Added by Surya for PS-114 to fetch the Product using CouchBase client - Start*/

    public Optional<Product> getByTPNBWithCouchBaseClient(String tpnb,CouchbaseClient couchbaseClient) {
        Product product = new Product();
        String productJson = (String) couchbaseClient.get(getProductKey(tpnb));

        if(productJson == null){
            product = null;
        }
        else{
            try {
                product = mapper.readValue(productJson,Product.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (product != null) ? Optional.of(product) : Optional.<Product>absent();
    }

    public String getMappedTPNCorTPNBWithCouchBaseClient(String tpn,CouchbaseClient couchbaseClient) throws CouchbaseOperationException {
        String tpnVal = null;
        if(!Dockyard.isSpaceOrNull(tpn)) {
            tpnVal = (String) couchbaseClient.get(tpn);
            if(!Dockyard.isSpaceOrNull(tpnVal))
                tpnVal = tpnVal.replace("\"", "");
        }
        return tpnVal;
    }
            /*Added by Surya for PS-114 to fetch the Product using CouchBase client - End*/

}

