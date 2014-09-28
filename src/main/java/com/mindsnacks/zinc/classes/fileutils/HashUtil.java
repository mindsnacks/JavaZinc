package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author John Ericksen
 */
public class HashUtil {

    private static final String HASH_TYPE = "SHA1";
    private static final int BUFF_SIZE = 1024;

    public String sha1HashString(String input){
        return sha1HashString(new ByteArrayInputStream(input.getBytes()));
    }

    public String sha1HashString(InputStream inputStream) {
        return toHexString(sha1Hash(inputStream));
    }

    private MessageDigest sha1Hash(InputStream inputStream) {
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

    public MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(HASH_TYPE);
        } catch (NoSuchAlgorithmException e) {
            throw new ZincRuntimeException("Digest Hash " + HASH_TYPE + " Unavailable", e);
        }
    }

    private String toHexString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();

        for (byte b : digest.digest()) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

    public ValidatingDigestOutputStream wrapOutputStreamWithDigest(OutputStream outputStream){
        return new ValidatingDigestOutputStream(outputStream, newDigest());
    }
}
