package com.mindsnacks.zinc.classes.fileutils;

import com.google.common.io.Files;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class FileHelper {
    public static final int BUFFER_SIZE = 8192;

    public void unzipFile(final File originFolder, final String originFilename, final File destinationFolder, final String destinationFilename) throws IOException {
        final File input = new File(originFolder, originFilename),
                   output = new File(destinationFolder, destinationFilename);

        if (!output.exists()) {
            createDirectories(output);

            final InputStream in;

            try {
                in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(input)));
            } catch (ZipException e) {
                throw new ZincRuntimeException("Error opening gzip file: " + input.getAbsolutePath(), e);
            }

            final OutputStream dest = new BufferedOutputStream(new FileOutputStream(output));
            try {
                int count;
                final byte data[] = new byte[BUFFER_SIZE];

                while ((count = in.read(data)) != -1) {
                    dest.write(data, 0, count);
                }
            } finally {
                dest.close();
                in.close();
            }
        }
    }

    public Reader readerForFile(final File file) throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    public boolean moveFile(final File originFolder, final String originFilename, final File destinationFolder, final String destinationFilename) {
        final File input = new File(originFolder, originFilename),
                   output = new File(destinationFolder, destinationFilename);

        return input.renameTo(output);
    }

    public void copyFile(final File originFolder, final String originFilename, final File destinationFolder, final String destinationFilename) throws IOException {
        final File input = new File(originFolder, originFilename),
                   output = new File(destinationFolder, destinationFilename);

        if (!output.exists()) {
            createDirectories(output);
            Files.copy(input, output);
        }
    }

    public boolean removeFile(final File file) {
        return file.delete();
    }

    private boolean createDirectories(final File file) {
        return file.getParentFile().mkdirs();
    }
}
