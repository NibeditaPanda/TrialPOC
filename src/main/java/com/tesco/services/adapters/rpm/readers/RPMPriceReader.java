package com.tesco.services.adapters.rpm.readers;

import com.tesco.services.adapters.rpm.dto.PriceDTO;

public interface RPMPriceReader {
    public PriceDTO getNext();
}
