package com.tesco.services.repositories;

import com.couchbase.client.CouchbaseClient;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.CouchbaseWrapper;
import com.tesco.couchbase.listeners.GetListener;
import com.tesco.couchbase.listeners.Listener;
import com.tesco.couchbase.listeners.SetListener;
import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.Store;
import com.tesco.services.exceptions.InvalidDataException;
import org.slf4j.Logger;

import java.io.IOException;

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
            if(isSpaceOrNull(couchbaseWrapper.get(TPNC)))
                couchbaseWrapper.set(TPNC, itemJson);
            if(isSpaceOrNull(couchbaseWrapper.get(ITEM)))
                couchbaseWrapper.set(ITEM, tpncJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public String getProductTPNC(String item) {

        String tpnc = (String)couchbaseWrapper.get(item);
        tpnc = replaceOldValCharWithNewVal(tpnc,"\"","");
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
    /*Added By Nibedita - Null/space check - start*/
    public  boolean isSpaceOrNull(Object obj)
    {
        if(obj == " " || obj == null)
            return true;
        else
            return false;
    }
    /*Added By Nibedita - Null/space check - end*/
    /*Added By Nibedita - PS 78 - fetch item/tpnc based on tpnc/item input  - Start*/
    public String getMappedTPNCorTPNB(String tpn){
        String tpnVal = null;
        try {
              tpnVal = (String)couchbaseWrapper.get(tpn);
            tpnVal = tpnVal.replace("\"","");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tpnVal;
    }
    /*Added By Nibedita - PS 78 - fetch item/tpnc based on tpnc/item input  - Start*/
    /*Added By Nibedita - Null check and replace old char with new - Start*/
    public String replaceOldValCharWithNewVal(String value, String oldChar, String newChar)
    {
        if(!isSpaceOrNull(value) && value.contains(oldChar))
            return(value.replace(oldChar,newChar));
        else
            return value;

    }
    /*Added By Nibedita -  Null check and replace old char with new - Start*/
}
