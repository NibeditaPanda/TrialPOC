package com.tesco.adapters.rpm;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;

public class RPMPromotionCSVFileReader {
    private CSVReader csvReader;
    private int itemNumberIndex;
    private int zoneIndex;
    private int offerNameIndex;
    private int startDateIndex;
    private int endDateIndex;
    private int cfDesc1Index;
    private int cfDesc2Index;

    public RPMPromotionCSVFileReader(String filePath) throws IOException {
        csvReader = new CSVReader(new FileReader(filePath));

        List<String> headers = Arrays.asList(csvReader.readNext());
        itemNumberIndex = headers.indexOf("TPNB");
        zoneIndex = headers.indexOf("ZONE_ID");
        offerNameIndex = headers.indexOf("OFFER_NAME");
        startDateIndex = headers.indexOf("START_DATE");
        endDateIndex = headers.indexOf("END_DATE");
        cfDesc1Index = headers.indexOf("CF_DESC1");
        cfDesc2Index = headers.indexOf("CF_DESC2");
    }

    public DBObject getNext() throws IOException {
        String[] nextline = csvReader.readNext();

        if (nextline == null) {
            return null;
        }

        BasicDBObject promotion = new BasicDBObject();
        promotion.put(ITEM_NUMBER, nextline[itemNumberIndex]);
        promotion.put(ZONE_ID, nextline[zoneIndex]);
        promotion.put(PROMOTION_OFFER_NAME, nextline[offerNameIndex]);
        promotion.put(PROMOTION_START_DATE, nextline[startDateIndex]);
        promotion.put(PROMOTION_END_DATE, nextline[endDateIndex]);
        promotion.put(PROMOTION_CF_DESCRIPTION_1, nextline[cfDesc1Index]);
        promotion.put(PROMOTION_CF_DESCRIPTION_2, nextline[cfDesc2Index]);

        return promotion;
    }
}
