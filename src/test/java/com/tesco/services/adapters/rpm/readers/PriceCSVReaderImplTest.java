package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import org.junit.Test;

import java.util.HashMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PriceCSVReaderImplTest {
    final String itemHeader = "ITEM";
    final String priceZoneIdHeader = "PRICE_ZONE_ID";
    final String sellingRetailHeader = "SELLING_RETAIL";

    @Test
    public void shouldGetRecord() throws Exception {
        final String filePath = "./src/test/java/com/tesco/services/adapters/rpm/readers/fixtures/single_price_zone.csv";
        PriceCSVReaderImpl csvPriceReader = new PriceCSVReaderImpl(filePath, itemHeader, priceZoneIdHeader, sellingRetailHeader);

        assertThat(csvPriceReader.getNext()).isEqualTo(getHeaderToValueMap("122223", "1", "1.5"));
        assertThat(csvPriceReader.getNext()).isNull();
    }

    @Test
    public void shouldGetMultipleRecords() throws Exception {
        final String filePath = "./src/test/java/com/tesco/services/adapters/rpm/readers/fixtures/multiple_price_zone.csv";
        PriceCSVReaderImpl csvPriceReader = new PriceCSVReaderImpl(filePath, itemHeader, priceZoneIdHeader, sellingRetailHeader);

        assertThat(csvPriceReader.getNext()).isEqualTo(getHeaderToValueMap("122223", "1", "1.5"));
        assertThat(csvPriceReader.getNext()).isEqualTo(getHeaderToValueMap("122223-001", "1", "1.7"));
        assertThat(csvPriceReader.getNext()).isNull();
    }

    @Test
    public void shouldIgnoreUnwantedHeaders() throws Exception {
        final String filePath = "./src/test/java/com/tesco/services/adapters/rpm/readers/fixtures/single_price_zone.csv";
        PriceCSVReaderImpl csvPriceReader = new PriceCSVReaderImpl(filePath, itemHeader, priceZoneIdHeader, sellingRetailHeader);

        assertThat(csvPriceReader.getNext()).doesNotContainKey("EXTRA_HEADER");
    }

    @Test(expected = ColumnNotFoundException.class)
    public void shouldThrowExceptionForMissingHeader() throws Exception {
        final String filePath = "./src/test/java/com/tesco/services/adapters/rpm/readers/fixtures/single_price_zone.csv";
        new PriceCSVReaderImpl(filePath, itemHeader, priceZoneIdHeader, sellingRetailHeader, "MISSING_HEADER");
    }

    @Test
    public void shouldCloseReaderAfterReading() throws Exception {
        CSVReader csvReaderMock = mock(CSVReader.class);
        when(csvReaderMock.readNext()).thenReturn(new String[]{}).thenReturn(null);
        PriceCSVReaderImpl csvPriceReader = new PriceCSVReaderImpl(csvReaderMock, new String[]{});

        csvPriceReader.getNext();

        verify(csvReaderMock).close();
    }

    private HashMap<String, String> getHeaderToValueMap(String tpnb, String zoneId, String price) {
        final HashMap<String, String> expected = new HashMap<>();
        expected.put(itemHeader, tpnb);
        expected.put(priceZoneIdHeader, zoneId);
        expected.put(sellingRetailHeader, price);
        return expected;
    }
}
