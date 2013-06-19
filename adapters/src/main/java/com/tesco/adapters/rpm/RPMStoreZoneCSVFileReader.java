package com.tesco.adapters.rpm;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;

public class RPMStoreZoneCSVFileReader {
    private final CSVReader csvReader;
    private final int storeIndex;
    private final int zoneIdIndex;
    private final int currencyIndex;

    public RPMStoreZoneCSVFileReader(String filePath) throws IOException {
        csvReader = new CSVReader(new FileReader(filePath));

        List<String> headers = Arrays.asList(csvReader.readNext());
        storeIndex = headers.indexOf("STORE");
        zoneIdIndex = headers.indexOf("ZONE_ID");
        currencyIndex = headers.indexOf("CURRENCY_CODE");
    }

    public DBObject getNext() throws IOException {
        String[] nextline = csvReader.readNext();

        if(nextline == null){
            return null;
        }

        String storeId = nextline[storeIndex];
        String zoneId = nextline[zoneIdIndex];
        String currency = nextline[currencyIndex];

        DBObject store = new BasicDBObject();
        store.put(STORE_ID, storeId);
        store.put(ZONE_ID, zoneId);
        store.put(CURRENCY, currency);

        return store;
    }
}
