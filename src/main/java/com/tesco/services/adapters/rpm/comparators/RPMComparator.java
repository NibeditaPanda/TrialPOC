package com.tesco.services.adapters.rpm.comparators;

import com.tesco.adapters.core.models.Product;

public class RPMComparator {
    public boolean compare(Product product1, Product product2) {
        if (product1 == null || product2 == null)
            return false;

        if (compareField(product1.getItemNumber(), product2.getItemNumber()))
            return false;

        if (compareField(product1.getTpnc(), product2.getTpnc()))
            return false;

        if (compareField(product1.getTpna(), product2.getTpna()))
            return false;

        if (compareField(product1.getDescription(), product2.getDescription()))
            return false;

        if (compareField(product1.getCustomerFriendlyDescription(), product2.getCustomerFriendlyDescription()))
            return false;

        if (compareField(product1.getTpnb(), product2.getTpnb()))
            return false;

        if (compareField(product1.getSellingByUnitOfMeasure(), product2.getSellingByUnitOfMeasure()))
            return false;

        if (compareField(product1.getSellByType(), product2.getSellByType()))
            return false;

        if (compareField(product1.getUkEPWIndicator(), product2.getUkEPWIndicator()))
            return false;

        if (compareField(product1.getRoiEPWIndicator(), product2.getRoiEPWIndicator()))
            return false;

        if (compareField(product1.getStatus(), product2.getStatus()))
            return false;

        if (compareField(product1.getBrand(), product2.getBrand()))
            return false;

        if (compareField(product1.getTillDescription(), product2.getTillDescription()))
            return false;

        return true;
    }

    private boolean compareField(Object obj1, Object obj2) {
        return (obj1 != null ? !obj1.equals(obj2) : obj2 != null);
    }
}
