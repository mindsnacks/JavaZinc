package com.mindsnacks.zinc.exceptions;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRuntimeException extends RuntimeException {
    public ZincRuntimeException(final String message) {
        super(message);
    }

    public ZincRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
