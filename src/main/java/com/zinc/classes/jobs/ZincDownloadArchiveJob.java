package com.zinc.classes.jobs;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import com.zinc.classes.ZincLogging;

import java.io.*;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadArchiveJob extends AbstractZincDownloadFileJob {
    public static final int BUFFER_SIZE = 2048;

    public ZincDownloadArchiveJob(final ZincRequestExecutor requestExecutor,
                                  final URL url,
                                  final File root,
                                  final String child,
                                  final boolean override) {
        super(requestExecutor, url, root, child, override);
    }

    @Override
    protected void writeFile(final InputStream inputStream, final File file) throws IOException {
        ZincLogging.log("Untaring " + file.getAbsolutePath());

        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new DownloadFileError("Error creating folder: " + file.getAbsolutePath());
            }
        }

        final TarInputStream tis = new TarInputStream(new BufferedInputStream(inputStream));

        try {
            TarEntry entry;
            while ((entry = tis.getNextEntry()) != null) {
                int count;
                final byte data[] = new byte[BUFFER_SIZE];

                final FileOutputStream fos = new FileOutputStream(file.getPath() + "/" + entry.getName());
                final BufferedOutputStream dest = new BufferedOutputStream(fos);

                try {
                    while ((count = tis.read(data)) != -1) {
                        dest.write(data, 0, count);
                    }
                } finally {
                    dest.close();
                }
            }
        } finally {
            ZincLogging.log("Finished untaring " + file.getAbsolutePath());

            tis.close();
        }
    }
}
