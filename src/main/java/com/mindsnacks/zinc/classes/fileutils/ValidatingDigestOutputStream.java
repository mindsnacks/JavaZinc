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
        String hash = toHexString(getMessageDigest());
        if (!hash.equals(expectedHash)) {
            throw new HashFailedException("File hash (" + hash + ") does not match expected hash (" + expectedHash + ").");
        }
    }

    public String toHexString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();
        for (byte b : digest.digest()) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static final class HashFailedException extends ZincException {

        public HashFailedException(String message) {
            super(message);
        }
    }
}
