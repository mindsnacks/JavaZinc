package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author John Ericksen
 */
public class HashUtil {

    private static final String HASH_TYPE = "SHA1";

    public MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(HASH_TYPE);
        } catch (NoSuchAlgorithmException e) {
            throw new HashUtilRuntimeException("Digest Hash " + HASH_TYPE + " Unavailable", e);
        }
    }

    public ValidatingDigestOutputStream wrapOutputStreamWithDigest(OutputStream outputStream) {
        return new ValidatingDigestOutputStream(outputStream, newDigest());
    }

    public class HashUtilRuntimeException extends ZincRuntimeException {
        public HashUtilRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
