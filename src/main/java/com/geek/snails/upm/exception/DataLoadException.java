package com.geek.snails.upm.exception;

public class DataLoadException extends RuntimeException {

    public DataLoadException(String msg, Exception e) {
        super(msg, e);
    }
}
