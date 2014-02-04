package com.tesco.services.adapters.rpm.readers;

import com.tesco.services.adapters.rpm.dto.StoreDTO;

import java.io.IOException;

public interface RPMStoreZoneReader {
    public StoreDTO getNext() throws IOException;
}
