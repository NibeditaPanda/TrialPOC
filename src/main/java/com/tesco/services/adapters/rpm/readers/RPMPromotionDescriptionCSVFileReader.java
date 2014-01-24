package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.core.Promotion;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static com.tesco.services.adapters.core.utils.ExtractionUtils.getHeader;
import static com.tesco.services.core.PriceKeys.*;

public class RPMPromotionDescriptionCSVFileReader implements RPMCSVFileReader {
    private final CSVReader csvReader;
    private final int offerIndex;
    private final int zoneIDIndex;
    private final int cfDesc1Index;
    private final int cfDesc2Index;
    private final int tpnbIndex;

    public RPMPromotionDescriptionCSVFileReader(String filePath) throws IOException, ColumnNotFoundException {
        csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ',');

        List<String> headers = Arrays.asList(csvReader.readNext());

        offerIndex = getHeader(headers, "OFFER_ID");
        zoneIDIndex = getHeader(headers, "ZONE_ID");
        tpnbIndex = getHeader(headers, "TPNB");
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

    public Promotion getNextDG() throws IOException {

        String[] nextLine = csvReader.readNext();

        if (nextLine == null) {
            return null;
        }

        Promotion promotion = new Promotion();
        promotion.setOfferId(nextLine[offerIndex]);
        promotion.setZoneId(nextLine[zoneIDIndex]);
        promotion.setItemNumber(nextLine[tpnbIndex]);
        promotion.setCFDescription1(nextLine[cfDesc1Index]);
        promotion.setCFDescription2(nextLine[cfDesc2Index]);

        return promotion;
    }
}
