package com.tesco.services.resources;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ahampson
 * Date: 19/08/2013
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeWrapper {
    public void exec(String command) throws IOException {
        Runtime.getRuntime().exec(command);
    }
}
