package com.tesco.services.repositories;

import java.util.UUID;

public class UUIDGenerator {

    public String getUUID() {
        return UUID.randomUUID().toString();
    }
}
