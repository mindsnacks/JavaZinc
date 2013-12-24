package com.mindsnacks.zinc.classes.jobs;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

import java.io.*;
import java.net.URL;

/**
 * @author NachoSoto
 *
 * This job downloads an archive and untars it.
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
    protected void writeFile(final InputStream inputStream, final File outputFile) throws IOException {
        logMessage("untaring " + outputFile.getAbsolutePath());

        createFolder(outputFile);

        final TarInputStream tis = new TarInputStream(new BufferedInputStream(inputStream));

        try {
            TarEntry entry;
            while ((entry = tis.getNextEntry()) != null) {
                untarEntry(outputFile, tis, entry);
            }
        } finally {
            tis.close();

            logMessage("Finished untaring " + outputFile.getAbsolutePath());
        }
    }

    private void untarEntry(final File outputFile,
                            final TarInputStream inputStream,
                            final TarEntry entry) throws IOException {
        int count;
        final byte data[] = new byte[BUFFER_SIZE];

        final FileOutputStream fos = new FileOutputStream(new File(outputFile, entry.getName()));
        final BufferedOutputStream dest = new BufferedOutputStream(fos);

        try {
            while ((count = inputStream.read(data)) != -1) {
                dest.write(data, 0, count);
            }
        } finally {
            dest.close();
        }
    }

    private void createFolder(final File outputFile) {
        if (!outputFile.exists() && !outputFile.mkdirs()) {
            throw new DownloadFileError("Error creating folder: " + outputFile.getAbsolutePath());
        }
    }
}
