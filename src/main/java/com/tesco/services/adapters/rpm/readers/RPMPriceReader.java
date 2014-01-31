package com.tesco.services.adapters.rpm.readers;

import com.tesco.services.adapters.rpm.dto.PriceDTO;

import java.io.IOException;

public interface RPMPriceReader {
    public PriceDTO getNext() throws IOException;
}