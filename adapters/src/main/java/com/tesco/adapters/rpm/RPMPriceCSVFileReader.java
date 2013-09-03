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

public class RPMPriceCSVFileReader implements RPMCSVFileReader {

    private final CSVReader csvReader;
    private final int itemNumberIndex;
    private final int zoneIdIndex;
    private final int priceIndex;
    private final int promotionalPriceIndex;

    public RPMPriceCSVFileReader(String filePath) throws IOException {
        csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ',');

        List<String> headers = Arrays.asList(csvReader.readNext());
        itemNumberIndex = headers.indexOf("ITEM");
        zoneIdIndex = headers.indexOf("ZONE_ID");
        priceIndex = headers.indexOf("SELLING_RETAIL");
        promotionalPriceIndex = headers.indexOf("SIMPLE_PROMO_RETAIL");
    }

    public DBObject getNext() throws IOException {
        String[] nextline = csvReader.readNext();

        if(nextline == null){
            return null;
        }

        String itemNumber = nextline[itemNumberIndex];
        String zoneId = nextline[zoneIdIndex];
        String price = nextline[priceIndex];
        String promotionalPrice = nextline[promotionalPriceIndex];

        DBObject priceObject = new BasicDBObject();
        priceObject.put(ITEM_NUMBER, itemNumber);
        priceObject.put(String.format("%s.%s.%s", ZONES, zoneId, PRICE), price);
        priceObject.put(String.format("%s.%s.%s", ZONES, zoneId, PROMO_PRICE), promotionalPrice);

        return priceObject;
    }
}
