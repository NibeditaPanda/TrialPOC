package com.tesco.services.utility;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

/**
 * Created by Nibedita on 06/08/2014.
 */
public class Dockyard {

    /*To get the sysdate with the required format*/
    public static String getSysDate(String dateFormatIn) {
        DateFormat dateFormat = new SimpleDateFormat(dateFormatIn);
        Date date = new Date();
        return dateFormat.format(date);

    }
    /*To check null value*/
   public static boolean isSpaceOrNull(Object obj) {

        if(obj == " " || obj == null || obj == "")
            return true;
        else
            return false;
    }
    /*To check Null and replace old char with new */
    public static String replaceOldValCharWithNewVal(String value, String oldChar, String newChar) {

        if(!Dockyard.isSpaceOrNull(value) && value.contains(oldChar))
            return(value.replace(oldChar,newChar));
        else
            return value;

    }

    /**
     * This will Scale the price based on the currency provided based on ISO4217 format.
     * @param currency - based on currency scale of the BigDecimal value to be returned
     * @param price - amount which needs to be rounded off.
     * @return - returns String value
     */
    public static String priceScaleRoundHalfUp(String currency, String price){
        String roundedPrice = null;
        if(!isSpaceOrNull(price) && !isSpaceOrNull(currency)){
            Currency cur = Currency.getInstance(currency);
            roundedPrice = String.valueOf(new BigDecimal(price).setScale(cur.getDefaultFractionDigits(), BigDecimal.ROUND_HALF_UP));
        }
        return roundedPrice;
    }
}
