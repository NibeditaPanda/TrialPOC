package com.tesco.adapters.rpm;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;

public class RPMPriceCSVFileReader {

    private final CSVReader csvReader;
    private final int itemNumberIndex;
    private final int zoneIdIndex;
    private final int nationalPriceIndex;

    public RPMPriceCSVFileReader(String rpmPriceCsvFile) throws IOException {
        csvReader = new CSVReader(new FileReader(rpmPriceCsvFile));

        List<String> UDAHeaders = Arrays.asList(csvReader.readNext());
        itemNumberIndex = UDAHeaders.indexOf("ITEM");
        zoneIdIndex = UDAHeaders.indexOf("ZONE_ID");
        nationalPriceIndex = UDAHeaders.indexOf("SELLING_RETAIL");
    }

    public DBObject getNext() throws IOException {
        String[] nextline = csvReader.readNext();

        if(nextline == null){
            return null;
        }

        String itemNumber = nextline[itemNumberIndex];
        String zoneId = nextline[zoneIdIndex];
        String nationalPrice = nextline[nationalPriceIndex];

        DBObject price = new BasicDBObject();
        price.put(ITEM_NUMBER, itemNumber);
        price.put(ZONE_ID, zoneId);
        price.put(NATIONAL_PRICE, nationalPrice);

        return price;
    }
}
