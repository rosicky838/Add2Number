package com.add2numweb.exception;

public class WorkOrderIdGenerationException extends RuntimeException {

    public WorkOrderIdGenerationException(String message) {
        super(message);
    }

    public WorkOrderIdGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
