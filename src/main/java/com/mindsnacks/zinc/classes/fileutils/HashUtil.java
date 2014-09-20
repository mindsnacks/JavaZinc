package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author John Ericksen
 */
public class HashUtil {

    private static final String HASH_TYPE = "SHA1";
    private static final int BUFF_SIZE = 1024;

    public static String sha1HashString(File file) {
        return toHexString(sha1Hash(file));
    }

    public static MessageDigest sha1Hash(File file) {
        try {
            // calculate sha1 hash
            MessageDigest md = newDigest();

            InputStream in = new FileInputStream(file);
            byte[] buff = new byte[BUFF_SIZE];
            int length;
            while ((length = in.read(buff)) > 0) {
                md.update(buff, 0, length);
            }
            in.close();

            return md;


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(HASH_TYPE);
        } catch (NoSuchAlgorithmException e) {
            throw new ZincRuntimeException("Digest Hash " + HASH_TYPE + " Unavailable", e);
        }
    }

    public static String toHexString(MessageDigest digest) {
        // convert to hex
        StringBuilder builder = new StringBuilder();

        for (byte b : digest.digest()) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }
}
