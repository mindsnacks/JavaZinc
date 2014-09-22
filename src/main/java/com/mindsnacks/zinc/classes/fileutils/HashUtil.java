package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author John Ericksen
 */
public final class HashUtil {

    private static final String HASH_TYPE = "SHA1";
    private static final int BUFF_SIZE = 1024;

    private HashUtil() {
        // private utility class constructor
    }

    public static String sha1HashString(InputStream inputStream) {
        return toHexString(sha1Hash(inputStream));
    }

    private static MessageDigest sha1Hash(InputStream inputStream) {
        try {
            MessageDigest md = newDigest();

            byte[] buff = new byte[BUFF_SIZE];
            int length;
            while ((length = inputStream.read(buff)) > 0) {
                md.update(buff, 0, length);
            }
            inputStream.close();

            return md;
        } catch (IOException e) {
            throw new ZincRuntimeException("Could not read file.", e);
        }
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(HASH_TYPE);
        } catch (NoSuchAlgorithmException e) {
            throw new ZincRuntimeException("Digest Hash " + HASH_TYPE + " Unavailable", e);
        }
    }

    private static String toHexString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();

        for (byte b : digest.digest()) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }
}
