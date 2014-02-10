package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tesco.services.adapters.core.utils.ExtractionUtils.getHeaderIndex;
import static java.util.Arrays.asList;

public class CSVPriceReaderImpl implements PriceCSVReader {

    private final CSVReader csvReader;
    private Map<String, Integer> headerIndex = new HashMap<>();

    public CSVPriceReaderImpl(String filePath, String... headers) throws IOException, ColumnNotFoundException {
        this(new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ','), headers);
    }

    CSVPriceReaderImpl(CSVReader csvReader, String[] headers) throws IOException, ColumnNotFoundException {
        this.csvReader = csvReader;
        List<String> headersInCSVFile = asList(csvReader.readNext());

        for (String header : headers) {
            headerIndex.put(header, getHeaderIndex(headersInCSVFile, header));
        }
    }

    @Override
    public Map<String, String> getNext() throws IOException {
        String[] nextLine = csvReader.readNext();

        if (nextLine == null) {
            csvReader.close();
            return null;
        }

        Map<String, String> headerToValue = new HashMap<>();

        for (String header : headerIndex.keySet()) {
            headerToValue.put(header, nextLine[headerIndex.get(header)]);
        }

        return headerToValue;
    }
}
