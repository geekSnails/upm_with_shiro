package com.geek.snails.upm.exception;

public class RemoteHttpCallException extends RuntimeException {

    public RemoteHttpCallException(String msg){
        super(msg);
    }

    public RemoteHttpCallException(Exception e) {
        super(e);
    }
}
