package com.tesco.services.resources;

import java.io.IOException;

public class RuntimeWrapper {
    public void exec(String command) throws IOException {
        Runtime.getRuntime().exec(command);
    }
}
