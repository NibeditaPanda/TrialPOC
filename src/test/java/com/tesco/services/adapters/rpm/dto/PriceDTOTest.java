package com.tesco.services.adapters.rpm.dto;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class PriceDTOTest {
    @Test
    public void shouldGetTPNB() {
        PriceDTO priceDTO = new PriceDTO("0343433", "1", "3.4");
        assertThat(priceDTO.getTPNB()).isEqualTo("0343433");
    }

    @Test
    public void shouldGetTPNBWhenItemNumberHasVariantSuffix() {
        PriceDTO priceDTO = new PriceDTO("0343433-001", "1", "3.4");
        assertThat(priceDTO.getTPNB()).isEqualTo("0343433");
    }
}
