package com.tesco.services.adapters.rpm.comparators;


import com.tesco.services.core.Product;
import com.tesco.services.core.Store;

public class RPMComparator {
    //Compare two product and their TPNB
    public boolean compare(Product product1, Product product2) {
        if (product1 == null || product2 == null)
            return false;

        if (compareField(product1.getTPNB(), product2.getTPNB()))
            return false;

        if (compareField(product1.toString(), product2.toString()))
            return false;

        return true;
    }
    //Compare two store and their curreny , price zone id and promo zone id
    public boolean compare(Store store1, Store store2) {
        if (store1 == null || store2 == null)
            return false;

        if (compareField(store1.getStoreId(), store2.getStoreId()))
            return false;

        if (compareField(store1.getCurrency(), store2.getCurrency()))
            return false;

        if (compareField(store1.getPriceZoneId(), store2.getPriceZoneId()))
            return false;

        if (compareField(store1.getPromoZoneId(), store2.getPromoZoneId()))
            return false;

        return true;
    }

    private boolean compareField(Object obj1, Object obj2) {
        return (obj1 != null ? !obj1.equals(obj2) : obj2 != null);
    }
}
