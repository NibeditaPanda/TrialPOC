package com.tesco.services.adapters.core.utils;

import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;

import java.util.List;

/**
 * <p>
 *     Class created to catch ColumnNotFoundException with the missing column name
 *     to help identify the missing headers in the incoming files.
 *     Performed during Header Validation
 *     @author PriceService Team
 *
 *     @param List<String> headers
 *     @param String columnName
 *     @return columnIndex of the missing column.
 *     At the same time an exception is also thrown for the missing column.
 */
public final class ExtractionUtils {
    private ExtractionUtils() {
    }

    public static int getHeaderIndex(List<String> headers, String columnName) throws ColumnNotFoundException {
        int columnIndex = headers.indexOf(columnName);
        if (columnIndex == -1) {
            throw new ColumnNotFoundException(columnName + " column is not found in the Extract. The Import Process will Abort");
        }

        return columnIndex;
    }
}