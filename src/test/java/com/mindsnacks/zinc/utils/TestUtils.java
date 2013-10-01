package com.mindsnacks.zinc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class TestUtils {
    public static String readFile(final String path) throws IOException {
        final File file = new File(path);
        final FileInputStream fis = new FileInputStream(file);

        final byte[] bytes = new byte[(int)file.length()];

        fis.read(bytes);
        fis.close();

        return new String(bytes);
    }

    public static String readFile(final File file) throws IOException {
        return readFile(file.getPath());
    }
}
