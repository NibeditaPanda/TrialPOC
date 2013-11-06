package com.tesco.adapters.rpm.readers;

import com.mongodb.DBObject;

import java.io.IOException;

public interface RPMCSVFileReader {
    public DBObject getNext() throws IOException;
}
