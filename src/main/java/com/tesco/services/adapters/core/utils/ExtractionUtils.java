package com.tesco.services.adapters.core.utils;

import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;

import java.util.List;

public class ExtractionUtils {
    private ExtractionUtils() {
    }

    public static int getHeader(List<String> headers, String columnName) throws ColumnNotFoundException {
        int columnIndex = headers.indexOf(columnName);
        if (columnIndex == -1) {
            throw new ColumnNotFoundException(columnName + " is not found");
        }

        return columnIndex;
    }
}