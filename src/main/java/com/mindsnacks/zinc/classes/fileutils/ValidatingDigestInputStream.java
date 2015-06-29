package com.mindsnacks.zinc.classes.fileutils;


import com.mindsnacks.zinc.exceptions.ZincException;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * @author Miguel Carranza
 */
public class ValidatingDigestInputStream extends DigestInputStream {

    public ValidatingDigestInputStream(InputStream stream, MessageDigest digest) {
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
