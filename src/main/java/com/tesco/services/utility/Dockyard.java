package com.tesco.services.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Nibedita on 06/08/2014.
 */
public class Dockyard {

    /*To get the sysdate with the required format*/
    public static String getSysDate(String date_format) {
        DateFormat dateFormat = new SimpleDateFormat(date_format);
        Date date = new Date();
        String sys_date = dateFormat.format(date);
        return sys_date;

    }
    /*To check null value*/
   public static boolean isSpaceOrNull(Object obj)
    {
        if(obj == " " || obj == null || obj == "")
            return true;
        else
            return false;
    }
    /*To check Null and replace old char with new */
    public static String replaceOldValCharWithNewVal(String value, String oldChar, String newChar)
    {
        if(!Dockyard.isSpaceOrNull(value) && value.contains(oldChar))
            return(value.replace(oldChar,newChar));
        else
            return value;

    }
}
