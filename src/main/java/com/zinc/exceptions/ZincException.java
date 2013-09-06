package com.zinc.exceptions;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincException extends Exception {
    public ZincException(final String message) {
        super(message);
    }

    public ZincException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
