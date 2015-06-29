package com.mindsnacks.zinc.classes.fileutils;


import com.mindsnacks.zinc.exceptions.ZincException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

/**
 * @author Miguel Carranza
 */
public class ValidatingDigestInputStream extends DigestInputStream {

    public ValidatingDigestInputStream(InputStream stream, MessageDigest digest) {
        super(stream, digest);
    }

    public void validate(String expectedHash) throws HashFailedException {
        final byte[] buffer = new byte[1024];

        try {
            for (int read = 0; (read = in.read(buffer)) != -1;) {
                digest.update(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
