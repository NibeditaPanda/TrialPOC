package com.tesco.services.adapters.core.utils;

import com.tesco.services.adapters.core.exceptions.ColumnNotFoundException;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;

public class ExtractionUtilsTest {
    @Test
    public void shouldGetHeaderIndex() throws ColumnNotFoundException {
        List<String> headers = asList("ITEM", "ZONE_ID");
        assertThat(ExtractionUtils.getHeaderIndex(headers, "ITEM")).isEqualTo(0);
        assertThat(ExtractionUtils.getHeaderIndex(headers, "ZONE_ID")).isEqualTo(1);
    }

    @Test(expected = ColumnNotFoundException.class)
    public void shouldThrowExceptionWhenHeaderNotFound() throws ColumnNotFoundException {
        List<String> headers = asList(new String[]{"ITEM"});
        ExtractionUtils.getHeaderIndex(headers, "ZONE_ID");
    }
}
