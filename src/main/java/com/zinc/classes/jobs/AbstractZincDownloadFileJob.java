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

    public AbstractZincDownloadFileJob(final ZincRequestExecutor requestExecutor, final URL url, final File root, final String child) {
        super(requestExecutor, url, File.class);
        mFile = new File(root, child);
    }

    @Override
    public final File call() throws DownloadFileError {
        final InputStream inputStream = mRequestExecutor.get(mUrl);

        try {
            writeFile(inputStream, mFile);
        } catch (IOException e) {
            throw new DownloadFileError("Error writing to file '" + mFile.getAbsolutePath() + "'", e);
        }

        return mFile;
    }

    abstract protected void writeFile(final InputStream inputStream, final File file) throws IOException;
}