package com.tesco.adapters.rpm;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;

public class RPMStoreCSVFileReader implements RPMCSVFileReader{
    private static final String PRICE_ZONE_FLAG = "1";
    private static final String PROMOTION_ZONE_FLAG = "2";
    private final CSVReader csvReader;
    private final int storeIndex;
    private final int zoneIdIndex;
    private final int currencyIndex;
    private final int zoneType;


    public RPMStoreCSVFileReader(String filePath) throws IOException {
        csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ',');

        List<String> headers = Arrays.asList(csvReader.readNext());
        storeIndex = headers.indexOf("STORE");
        zoneIdIndex = headers.indexOf("ZONE_ID");
        currencyIndex = headers.indexOf("CURRENCY_CODE");
        zoneType = headers.indexOf("ZONE_TYPE");
    }

    public DBObject getNext() throws IOException {
        String[] nextline = csvReader.readNext();

        if(nextline == null){
            return null;
        }

        String typeOfZone = nextline[zoneType];
        String storeId = nextline[storeIndex];
        String zoneId = nextline[zoneIdIndex];
        String currency = nextline[currencyIndex];

        DBObject store = new BasicDBObject();
        store.put(STORE_ID, storeId);
        if(typeOfZone.equals(PRICE_ZONE_FLAG)) {
            store.put(PRICE_ZONE_ID, zoneId);
        } else if(typeOfZone.equals(PROMOTION_ZONE_FLAG)) {
            store.put(PROMOTION_ZONE_ID, zoneId);
        }

        store.put(CURRENCY, currency);

        return store;
    }
}
