package com.tesco.services.adapters.rpm.readers;

import com.tesco.services.adapters.rpm.dto.StoreDTO;

public interface RPMStoreZoneReader {
    public StoreDTO getNext();
}
