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
       if(val.equals(null) || val.equals("") || val.equals(" "))
        flag = true;
       else
        flag = false;
        Assert.assertEquals(flag,Dockyard.isSpaceOrNull(val));
    }
}
