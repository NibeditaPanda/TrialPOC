package com.tesco.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.adapters.core.exceptions.ColumnNotFoundException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static com.tesco.adapters.core.PriceKeys.*;
import static com.tesco.adapters.core.utils.ExtractionUtils.getHeader;

public class RPMPromotionDescriptionCSVFileReader implements RPMCSVFileReader {
    private final CSVReader csvReader;
    private final int offerIndex;
    private final int zoneIDIndex;
    private final int cfDesc1Index;
    private final int cfDesc2Index;

    public RPMPromotionDescriptionCSVFileReader(String filePath) throws IOException, ColumnNotFoundException {
        csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ',');

        List<String> headers = Arrays.asList(csvReader.readNext());

        offerIndex = getHeader(headers, "OFFER_ID");
        zoneIDIndex = getHeader(headers, "ZONE_ID");
        cfDesc1Index = getHeader(headers, "CF_DESC1");
        cfDesc2Index = getHeader(headers, "CF_DESC2");
    }

    public DBObject getNext() throws IOException {

        String[] nextLine = csvReader.readNext();

        if (nextLine == null) {
            return null;
        }

        BasicDBObject promotion = new BasicDBObject();
        promotion.put(PROMOTION_OFFER_ID, nextLine[offerIndex]);
        promotion.put(ZONE_ID, nextLine[zoneIDIndex]);
        promotion.put(PROMOTION_CF_DESCRIPTION_1, nextLine[cfDesc1Index]);
        promotion.put(PROMOTION_CF_DESCRIPTION_2, nextLine[cfDesc2Index]);

        return promotion;
    }
}
