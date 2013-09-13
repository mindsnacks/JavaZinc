package com.zinc.classes.jobs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public abstract class AbstractZincDownloadFileJob extends AbstractZincDownloadJob<File> {
    private final File mFile;
    private final boolean mOverride;

    public AbstractZincDownloadFileJob(final ZincRequestExecutor requestExecutor,
                                       final URL url,
                                       final File root,
                                       final String child,
                                       final boolean override) {
        super(requestExecutor, url, File.class);
        mFile = new File(root, child);
        mOverride = override;
    }

    @Override
    public final File run() throws DownloadFileError {
        if (shouldDownloadFile()) {
            final InputStream inputStream = mRequestExecutor.get(mUrl);

            try {
                writeFile(inputStream, mFile);
            } catch (IOException e) {
                throw new DownloadFileError("Error writing to file '" + mFile.getAbsolutePath() + "'", e);
            }
        } else {
            logMessage("not downloading file - was already there.");
        }

        return mFile;
    }

    private boolean shouldDownloadFile() {
        return mOverride || !mFile.exists();
    }

    abstract protected void writeFile(final InputStream inputStream, final File file) throws IOException;
}
