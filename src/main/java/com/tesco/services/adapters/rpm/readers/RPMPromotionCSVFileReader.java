package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.core.Promotion;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.tesco.services.adapters.core.utils.ExtractionUtils.getHeaderIndex;
import static com.tesco.services.core.PriceKeys.*;
import static java.util.Arrays.asList;

public class RPMPromotionCSVFileReader implements RPMCSVFileReader {
    private CSVReader csvReader;
    private int itemNumberIndex;
    private int zoneIndex;
    private int offerNameIndex;
    private int startDateIndex;
    private int endDateIndex;
    private int offerIdIndex;

    public RPMPromotionCSVFileReader(String filePath) throws IOException, ColumnNotFoundException {
        csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ',');

        List<String> headers = asList(csvReader.readNext());

        itemNumberIndex = getHeaderIndex(headers, "TPNB");
        zoneIndex = getHeaderIndex(headers, "ZONE_ID");
        offerIdIndex = getHeaderIndex(headers, "OFFER_ID");
        offerNameIndex = getHeaderIndex(headers, "OFFER_NAME");
        startDateIndex = getHeaderIndex(headers, "START_DATE");
        endDateIndex = getHeaderIndex(headers, "END_DATE");
    }

    public DBObject getNext() throws IOException {
        String[] nextLine = csvReader.readNext();

        if (nextLine == null) {
            return null;
        }

        BasicDBObject promotion = new BasicDBObject();
        promotion.put(ITEM_NUMBER, nextLine[itemNumberIndex]);
        promotion.put(ZONE_ID, nextLine[zoneIndex]);
        promotion.put(PROMOTION_OFFER_ID, nextLine[offerIdIndex]);
        promotion.put(PROMOTION_OFFER_NAME, nextLine[offerNameIndex]);
        promotion.put(PROMOTION_START_DATE, nextLine[startDateIndex]);
        promotion.put(PROMOTION_END_DATE, nextLine[endDateIndex]);

        return promotion;
    }

    public Promotion getNextDG() throws IOException {
        String[] nextLine = csvReader.readNext();

        if (nextLine == null) {
            return null;
        }

        Promotion promotion = new Promotion();
        promotion.setItemNumber(nextLine[itemNumberIndex]);
        promotion.setZoneId(Integer.parseInt(nextLine[zoneIndex]));
        promotion.setOfferId(nextLine[offerIdIndex]);
        promotion.setOfferName(nextLine[offerNameIndex]);
        promotion.setStartDate(nextLine[startDateIndex]);
        promotion.setEndDate(nextLine[endDateIndex]);

        return promotion;
    }
}
