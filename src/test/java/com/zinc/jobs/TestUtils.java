package com.zinc.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class TestUtils {
    public static String readFile(String path) throws IOException {
        final File file = new File(path);
        final FileInputStream fis = new FileInputStream(file);

        final byte[] bytes = new byte[(int)file.length()];

        fis.read(bytes);

        return new String(bytes);
    }
}
