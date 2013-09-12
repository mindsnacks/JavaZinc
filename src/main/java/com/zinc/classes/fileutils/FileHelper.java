package com.zinc.classes.fileutils;

import com.google.common.io.Files;
import com.zinc.classes.data.ZincBundle;
import com.zinc.exceptions.ZincRuntimeException;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class FileHelper {
    public static final int BUFFER_SIZE = 8192;

    public void unzipFile(final ZincBundle bundle, final String filename, final String destination) throws IOException {
        final File input = new File(bundle, filename),
                   output = new File(bundle, destination);

        if (!output.exists()) {
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

    public boolean moveFile(final ZincBundle bundle, final String filename, final String destination) {
        return new File(bundle, filename).renameTo(new File(bundle, destination));
    }

    public void copyFile(final ZincBundle bundle, final String filename, final String destination) throws IOException {
        final File destinationFile = new File(bundle, destination);
        if (!destinationFile.exists()) {
            Files.copy(new File(bundle, filename), destinationFile);
        }
    }

    public void removeFile(final ZincBundle bundle, final String filename) {
        new File(bundle, filename).delete();
    }
}
