package com.mindsnacks.zinc.classes.fileutils;


import com.mindsnacks.zinc.exceptions.ZincException;

import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

/**
 * @author John Ericksen
 */
public class ValidatingDigestOutputStream extends DigestOutputStream {

    public ValidatingDigestOutputStream(OutputStream stream, MessageDigest digest) {
        super(stream, digest);
    }

    public void validate(String expectedHash) throws HashFailedException {
        String hash = MessageDigestUtils.toHexString(getMessageDigest());
        if (!hash.equals(expectedHash)) {
            throw new HashFailedException("File hash (" + hash + ") does not match expected hash (" + expectedHash + ").");
        }
    }

    public static final class HashFailedException extends ZincException {

        public HashFailedException(String message) {
            super(message);
        }
    }
}
