package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import com.tesco.services.adapters.rpm.dto.StoreDTO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.tesco.services.adapters.core.utils.ExtractionUtils.getHeaderIndex;
import static java.util.Arrays.asList;

public class RPMStoreZoneReaderImpl implements RPMStoreZoneReader {

    private final CSVReader csvReader;
    private final int storeIdIndex;
    private final int zoneIdIndex;
    private final int currencyCodeIndex;
    private final int zoneTypeIndex;

    public RPMStoreZoneReaderImpl(String filePath) throws IOException, ColumnNotFoundException {
        this(new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ','));
    }

    public RPMStoreZoneReaderImpl(CSVReader csvReader) throws IOException, ColumnNotFoundException {
        this.csvReader = csvReader;
        List<String> headers = asList(csvReader.readNext());

        storeIdIndex = getHeaderIndex(headers, "STORE");
        zoneIdIndex = getHeaderIndex(headers, "ZONE_ID");
        currencyCodeIndex = getHeaderIndex(headers, "CURRENCY_CODE");
        zoneTypeIndex = getHeaderIndex(headers, "ZONE_TYPE");
    }

    @Override
    public StoreDTO getNext() throws IOException {
        String[] nextLine = csvReader.readNext();

        if (nextLine == null) {
            csvReader.close();
            return null;
        }

        return new StoreDTO(Integer.parseInt(nextLine[storeIdIndex]),
                Integer.parseInt(nextLine[zoneIdIndex]),
                Integer.parseInt(nextLine[zoneTypeIndex]),
                nextLine[currencyCodeIndex]);
    }
}
