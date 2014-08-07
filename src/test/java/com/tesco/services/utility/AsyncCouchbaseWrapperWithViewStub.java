package com.tesco.services.utility;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.couchbase.testutils.AsyncCouchbaseWrapperStub;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.fest.util.Lists;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsyncCouchbaseWrapperWithViewStub extends AsyncCouchbaseWrapperStub {

    public AsyncCouchbaseWrapperWithViewStub(Map<String, ImmutablePair<Long, String>> db) {
        super(db);
    }

    @Override
    public Iterator<ViewResponse> paginatedQuery(View view, Query query, int docsPerPage) {
        List<ViewRow> allRows = getSortedViewRows();
        List<ViewResponse> pages = paginate(docsPerPage, allRows);
        return pages.listIterator();
    }

    private List<ViewRow> getSortedViewRows() {
        List<ViewRow> allRows = Lists.newArrayList();
        ObjectMapper mapper = new ObjectMapper();

        for (String key : db.keySet()) {
            String val = db.get(key).getRight();
            JsonNode json;
            try {
                json = mapper.readTree(val);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (json.isObject()) {
                JsonNode jsonItemNumber = json.findValue("itemNumber");
                if (jsonItemNumber != null) {
                    String itemNumber = jsonItemNumber.asText();

                    ViewRow viewRow = mock(ViewRow.class);
                    when(viewRow.getId()).thenReturn(key);
                    when(viewRow.getKey()).thenReturn(itemNumber);
                    allRows.add(viewRow);
                }
            }
        }

        Collections.sort(allRows, new Comparator<ViewRow>() {
            @Override
            public int compare(ViewRow o1, ViewRow o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return allRows;
    }

    private List<ViewResponse> paginate(int pageSize, List<ViewRow> allRows) {
        List<ViewResponse> pages = Lists.newArrayList();
        int i = 0;
        while (i < allRows.size()) {
            int end = i+pageSize;
            if (end > allRows.size()) {
                end = allRows.size();
            }
            List<ViewRow> page = allRows.subList(i, end);

            ViewResponse viewResponse = mock(ViewResponse.class);
            when(viewResponse.iterator()).thenReturn(page.listIterator());
            pages.add(viewResponse);

            i = i+pageSize;
        }
        return pages;
    }
}