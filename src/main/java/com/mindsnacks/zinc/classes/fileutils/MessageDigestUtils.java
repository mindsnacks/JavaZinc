package com.mindsnacks.zinc.classes.fileutils;

import java.security.MessageDigest;

/**
 * Created by Miguel Carranza on 6/29/15.
 */
public class MessageDigestUtils {
    public static String toHexString(final MessageDigest digest) {
        StringBuilder builder = new StringBuilder();
        for (byte b : digest.digest()) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
