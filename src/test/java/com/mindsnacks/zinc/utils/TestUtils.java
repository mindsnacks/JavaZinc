package com.mindsnacks.zinc.utils;

import com.google.common.io.CharStreams;
import org.junit.rules.TemporaryFolder;

import java.io.*;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class TestUtils {
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
}
