package com.tesco.services.utility;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by Nibedita on 06/08/2014.
 */
public class DockyardTest {
@Test
    public void checkNull()
    {
        String val = " ";
        boolean flag;
        //Modified by Pallavi as part of code refactor
       if(val==null || val=="" || val==" ")
        flag = true;
       else
        flag = false;
        Assert.assertEquals(flag,Dockyard.isSpaceOrNull(val));
    }

    @Test
    public void checkPriceScaleRoundForGBP(){
        String price1 = "11.2";
        String currency = "GBP";
        String price2 = "11";
        String price3 = "11.209";
        String price4 = "11.99999";
        String actualPrice = "11.20";
        String actualPrice2= "11.00";
        String actualPrice3= "11.21";
        String actualPrice4= "12.00";
        Assert.assertEquals(actualPrice, Dockyard.priceScaleRoundHalfUp(currency,price1));

        Assert.assertEquals(actualPrice2, Dockyard.priceScaleRoundHalfUp(currency,price2));

        Assert.assertEquals(actualPrice3, Dockyard.priceScaleRoundHalfUp(currency,price3));

        Assert.assertEquals(actualPrice4, Dockyard.priceScaleRoundHalfUp(currency,price4));
    }

    /**
     * IQD Iraqi dinar Decimal places=3
     */
    @Test
    public void checkPriceScaleRoundForIQD(){
        String price1 = "11.2";
        String currency = "IQD";
        String price2 = "11";
        String price3 = "11.209";
        String price4 = "11.99999";
        String actualPrice = "11.200";
        String actualPrice2= "11.000";
        String actualPrice3= "11.209";
        String actualPrice4= "12.000";
        Assert.assertEquals(actualPrice, Dockyard.priceScaleRoundHalfUp(currency,price1));

        Assert.assertEquals(actualPrice2, Dockyard.priceScaleRoundHalfUp(currency,price2));

        Assert.assertEquals(actualPrice3, Dockyard.priceScaleRoundHalfUp(currency,price3));

        Assert.assertEquals(actualPrice4, Dockyard.priceScaleRoundHalfUp(currency,price4));
    }

    /**
     * CLP Chile decimal places =0
     */
    @Test
    public void checkPriceScaleRoundForCLP(){
        String price1 = "11.2";
        String currency = "CLP";
        String price2 = "11";
        String price3 = "11.209";
        String price4 = "11.99999";
        String actualPrice = "11";
        String actualPrice4= "12";
        Assert.assertEquals(actualPrice, Dockyard.priceScaleRoundHalfUp(currency,price1));

        Assert.assertEquals(actualPrice, Dockyard.priceScaleRoundHalfUp(currency,price2));

        Assert.assertEquals(actualPrice, Dockyard.priceScaleRoundHalfUp(currency,price3));

        Assert.assertEquals(actualPrice4, Dockyard.priceScaleRoundHalfUp(currency,price4));
    }
}
