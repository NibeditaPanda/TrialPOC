package com.tesco.services.adapters.rpm.readers;

import au.com.bytecode.opencsv.CSVReader;
import com.tesco.services.adapters.rpm.dto.StoreDTO;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RPMStoreZoneReaderImplTest {
    @Test
    public void shouldGetPrice() throws Exception {
        RPMStoreZoneReaderImpl rpmStoreZoneReader = new RPMStoreZoneReaderImpl("./src/test/java/com/tesco/services/adapters/rpm/readers/fixtures/single_store_zone.csv");

        assertThat(rpmStoreZoneReader.getNext()).isEqualTo(new StoreDTO(2002, 1, 1, "GBP"));
        assertThat(rpmStoreZoneReader.getNext()).isNull();
    }

    @Test
    public void shouldGetMultiplePrices() throws Exception {
        RPMStoreZoneReaderImpl rpmStoreZoneReader = new RPMStoreZoneReaderImpl("./src/test/java/com/tesco/services/adapters/rpm/readers/fixtures/multiple_store_zone.csv");

        assertThat(rpmStoreZoneReader.getNext()).isEqualTo(new StoreDTO(2002, 1, 1, "GBP"));
        assertThat(rpmStoreZoneReader.getNext()).isEqualTo(new StoreDTO(2002, 5, 2, "GBP"));
        assertThat(rpmStoreZoneReader.getNext()).isNull();
    }

    @Test
    public void shouldCloseReaderAfterReading() throws Exception {
        CSVReader csvReaderMock = mock(CSVReader.class);
        when(csvReaderMock.readNext()).thenReturn(new String[]{"STORE","ZONE_ID","CURRENCY_CODE","ZONE_TYPE"}).thenReturn(null);
        RPMStoreZoneReaderImpl storeZoneReader = new RPMStoreZoneReaderImpl(csvReaderMock);

        storeZoneReader.getNext();

        verify(csvReaderMock).close();
    }
}
