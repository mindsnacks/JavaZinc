package com.mindsnacks.zinc.utils;

import com.google.common.io.CharStreams;
import com.mindsnacks.zinc.classes.fileutils.HashUtil;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.security.MessageDigest;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class TestUtils {

    private static final int BUFF_SIZE = 1024;

    public static String sha1HashString(String input) throws IOException {
        return sha1HashString(new ByteArrayInputStream(input.getBytes()));
    }

    public static String sha1HashString(InputStream inputStream) throws IOException {
        return toHexString(sha1Hash(inputStream));
    }

    private static MessageDigest sha1Hash(InputStream inputStream) throws IOException{
        MessageDigest md = new HashUtil().newDigest();

        byte[] buff = new byte[BUFF_SIZE];
        int length;
        while ((length = inputStream.read(buff)) > 0) {
            md.update(buff, 0, length);
        }
        inputStream.close();

        return md;
    }

    private static String toHexString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();

        for (byte b : digest.digest()) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

    public static String readFile(final String path) throws IOException {
        return readFile(new File(path));
    }

    public static String readFile(final File file) throws IOException {
        return CharStreams.toString(new BufferedReader(new FileReader(file)));
    }

    public static File createFile(final TemporaryFolder rootFolder,
                                  final String filename,
                                  final String contents) throws IOException {
        final File file = createFolderAndFile(rootFolder, filename);

        writeToFile(file, contents);

        return file;
    }

    private static File createFolderAndFile(final TemporaryFolder rootFolder,
                                            final String filename) throws IOException {
        final File folder = new File(rootFolder.getRoot(), filename).getParentFile();

        if (!folder.exists()) {
            assert folder.mkdirs();
        }

        return rootFolder.newFile(filename);
    }

    public static void writeToFile(final File file, String contents) throws IOException {
        final FileWriter writer = new FileWriter(file);

        try {
            writer.write(contents);
        } finally {
            writer.close();
        }
    }

    public static void createRandomFileInFolder(final File folder) throws IOException {
        TestUtils.writeToFile(
                new File(folder, TestFactory.randomString()),
                TestFactory.randomString());
    }
}
