package com.mindsnacks.zinc.classes.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadFileJob extends AbstractZincDownloadFileJob {
    private static final int BUFFER_SIZE = 1024;

    public ZincDownloadFileJob(final ZincRequestExecutor requestExecutor,
                               final URL url,
                               final File root,
                               final String child,
                               final boolean override) {
        super(requestExecutor, url, root, child, override);
    }

    // TODO: use FileHelper
    @Override
    protected void writeFile(final InputStream inputStream, final File file) throws IOException {
        logMessage("Saving file " + file.getAbsolutePath());

        final FileOutputStream outputStream = new FileOutputStream(file);

        int read = 0;
        final byte[] bytes = new byte[BUFFER_SIZE];

        while ((read = inputStream.read(bytes, 0, BUFFER_SIZE)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        outputStream.close();
    }
}
