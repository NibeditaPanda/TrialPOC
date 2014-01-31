package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.tesco.services.core.PriceKeys.*;
import static com.tesco.services.adapters.core.utils.ExtractionUtils.getHeaderIndex;
import static java.util.Arrays.asList;

public class RPMStoreZoneCSVFileReader implements RPMCSVFileReader {

    private static final String PRICE_ZONE_FLAG = "1";
    private static final String PROMOTION_ZONE_FLAG = "2";

    private final CSVReader csvReader;
    private final int storeIndex;
    private final int zoneIdIndex;
    private final int currencyIndex;
    private final int zoneType;

    public RPMStoreZoneCSVFileReader(String filePath) throws IOException, ColumnNotFoundException {
        csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ',');

        List<String> headers = asList(csvReader.readNext());

        storeIndex = getHeaderIndex(headers, "STORE");
        zoneIdIndex = getHeaderIndex(headers, "ZONE_ID");
        currencyIndex = getHeaderIndex(headers, "CURRENCY_CODE");
        zoneType = getHeaderIndex(headers, "ZONE_TYPE");
    }

    public DBObject getNext() throws IOException {
        String[] nextLine = csvReader.readNext();

        if (nextLine == null) {
            return null;
        }

        String typeOfZone = nextLine[zoneType];
        String storeId = nextLine[storeIndex];
        String zoneId = nextLine[zoneIdIndex];
        String currency = nextLine[currencyIndex];

        DBObject store = new BasicDBObject();
        store.put(STORE_ID, storeId);
        if (typeOfZone.equals(PRICE_ZONE_FLAG)) {
            store.put(PRICE_ZONE_ID, zoneId);
        } else if (typeOfZone.equals(PROMOTION_ZONE_FLAG)) {
            store.put(PROMOTION_ZONE_ID, zoneId);
        }

        store.put(CURRENCY, currency);

        return store;
    }
}
