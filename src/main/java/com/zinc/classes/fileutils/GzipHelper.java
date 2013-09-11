package com.zinc.classes.fileutils;

import com.zinc.classes.data.ZincBundle;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class GzipHelper {
    public static final int BUFFER_SIZE = 8192;

    public void unzipFile(final ZincBundle bundle, final String filename, final String destination) throws IOException {
        final File input = new File(bundle, filename),
                   output = new File(bundle, destination);

        if (!input.exists()) {
            throw new FileNotFoundException("File not found: " + input.getAbsolutePath());
        }

        final InputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(input)));;
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
