package com.tesco.services.adapters.rpm.readers;

import java.io.IOException;
import java.util.Map;
/**
 * To read the header data from csv file
 */
public interface PriceServiceCSVReader {
    public Map<String, String> getNext() throws IOException;
}
