package com.tesco.adapters.rpm;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;

public class RPMPromotionDescReader {
    private final CSVReader csvReader;
    private final int offerIndex;
    private final int zoneIDIndex;
    private final int cfDesc1Index;
    private final int cfDesc2Index;

    private DBObject next;

    public RPMPromotionDescReader(String filePath) throws IOException {
        csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ',');

        List<String> headers = Arrays.asList(csvReader.readNext());

        offerIndex = headers.indexOf("OFFER_ID");
        zoneIDIndex = headers.indexOf("ZONE_ID");
        cfDesc1Index = headers.indexOf("CF_DESC1");
        cfDesc2Index = headers.indexOf("CF_DESC2");
    }

    public DBObject getNext() throws IOException {

        String[] nextline = csvReader.readNext();

        if (nextline == null) {
            return null;
        }

        BasicDBObject promotion = new BasicDBObject();
        promotion.put(PROMOTION_OFFER_ID, nextline[offerIndex]);
        promotion.put(ZONE_ID, nextline[zoneIDIndex]);
        promotion.put(PROMOTION_CF_DESCRIPTION_1, nextline[cfDesc1Index]);
        promotion.put(PROMOTION_CF_DESCRIPTION_2, nextline[cfDesc2Index]);

        return promotion;
    }
}
