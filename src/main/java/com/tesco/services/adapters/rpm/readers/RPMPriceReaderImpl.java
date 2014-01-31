package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.adapters.rpm.dto.PriceDTO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.tesco.services.adapters.core.utils.ExtractionUtils.getHeaderIndex;
import static java.util.Arrays.asList;

public class RPMPriceReaderImpl implements RPMPriceReader {

    private final CSVReader csvReader;
    private final int itemNumberIndex;
    private final int zoneIdIndex;
    private final int priceIndex;

    public RPMPriceReaderImpl(String filePath) throws IOException, ColumnNotFoundException {
        this(new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ','));
    }

    RPMPriceReaderImpl(CSVReader csvReader) throws IOException, ColumnNotFoundException {
        this.csvReader = csvReader;
        List<String> headers = asList(csvReader.readNext());

        itemNumberIndex = getHeaderIndex(headers, "ITEM");
        zoneIdIndex = getHeaderIndex(headers, "PRICE_ZONE_ID");
        priceIndex = getHeaderIndex(headers, "SELLING_RETAIL");
    }

    @Override
    public PriceDTO getNext() throws IOException {
        String[] nextLine = csvReader.readNext();

        if (nextLine == null) {
            csvReader.close();
            return null;
        }

        PriceDTO priceDTO = new PriceDTO(nextLine[itemNumberIndex], nextLine[zoneIdIndex], nextLine[priceIndex]);

        return priceDTO;
    }
}
