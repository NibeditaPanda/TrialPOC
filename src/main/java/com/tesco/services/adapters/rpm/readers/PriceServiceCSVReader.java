package com.tesco.services.adapters.rpm.readers;

import java.io.IOException;
import java.util.Map;

public interface PriceServiceCSVReader {
    public Map<String, String> getNext() throws IOException;
}
