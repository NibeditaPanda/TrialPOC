package com.tesco.services.exceptions;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String errmsg) {
        super(errmsg);
    }

    public InvalidDataException(String errmsg, Throwable e) {
        super(errmsg, e);
    }

    public InvalidDataException(Throwable e) {
        super(e);
    }
}
