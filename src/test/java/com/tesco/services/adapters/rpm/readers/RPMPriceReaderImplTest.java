package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.tesco.services.adapters.rpm.dto.PriceDTO;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RPMPriceReaderImplTest {
    @Test
    public void shouldGetPrice() throws Exception {
        RPMPriceReaderImpl rpmPriceReader = new RPMPriceReaderImpl("./src/test/java/com/tesco/services/adapters/rpm/readers/fixtures/single_price_zone.csv");

        assertThat(rpmPriceReader.getNext()).isEqualTo(new PriceDTO("122223", "1", "1.5"));
        assertThat(rpmPriceReader.getNext()).isNull();
    }

    @Test
    public void shouldGetMultiplePrices() throws Exception {
        RPMPriceReaderImpl rpmPriceReader = new RPMPriceReaderImpl("./src/test/java/com/tesco/services/adapters/rpm/readers/fixtures/multiple_price_zone.csv");

        assertThat(rpmPriceReader.getNext()).isEqualTo(new PriceDTO("122223", "1", "1.5"));
        assertThat(rpmPriceReader.getNext()).isEqualTo(new PriceDTO("122223-001", "1", "1.7"));
        assertThat(rpmPriceReader.getNext()).isNull();
    }

    @Test
    public void shouldCloseReaderAfterReading() throws Exception {
        CSVReader csvReaderMock = mock(CSVReader.class);
        when(csvReaderMock.readNext()).thenReturn(new String[]{"ITEM", "PRICE_ZONE_ID", "SELLING_RETAIL"}).thenReturn(null);
        RPMPriceReaderImpl rpmPriceReader = new RPMPriceReaderImpl(csvReaderMock);

        rpmPriceReader.getNext();

        verify(csvReaderMock).close();
    }
}
