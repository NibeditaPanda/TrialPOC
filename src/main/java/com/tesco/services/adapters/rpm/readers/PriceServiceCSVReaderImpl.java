package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tesco.services.adapters.core.utils.ExtractionUtils.getHeaderIndex;
import static java.util.Arrays.asList;
/**
 * implements PriceServiceCSVReader interface, aims to read header information from csv files
 */
public class PriceServiceCSVReaderImpl implements PriceServiceCSVReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceServiceCSVReaderImpl.class);

    private final CSVReader csvReader;
    private Map<String, Integer> headerIndex = new HashMap<>();
    /**
     * <p>
     * For reading the header information from csv files
     * </p>
     * @param filePath
     * @param headers
     * @throws IOException
     * @throws ColumnNotFoundException
     */
    public PriceServiceCSVReaderImpl(String filePath, String... headers) throws IOException, ColumnNotFoundException {
        this(new CSVReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"), ','),filePath, headers);
    }

    PriceServiceCSVReaderImpl(CSVReader csvReader, String filePath, String... headers) throws IOException, ColumnNotFoundException {
        this.csvReader = csvReader;
        List<String> headersInCSVFile = asList(csvReader.readNext());
        List<String> heardersRequiredForServices = new ArrayList();
        LOGGER.info("Headers in the "+filePath.substring(filePath.lastIndexOf("/")+1)+" extract are "+headersInCSVFile);

        for (String header : headers) {
            heardersRequiredForServices.add(header);
        }
        LOGGER.info("Headers Required by the Services for the extract "+filePath.substring(filePath.lastIndexOf("/")+1)+" are "+heardersRequiredForServices);
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
