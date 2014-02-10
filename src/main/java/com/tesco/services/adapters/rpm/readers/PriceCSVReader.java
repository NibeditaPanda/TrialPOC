package com.tesco.services.adapters.rpm.readers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface PriceCSVReader {
    public Map<String, String> getNext() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;
}
