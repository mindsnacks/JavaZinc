package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.exceptions.ZincException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadFileJob extends AbstractZincDownloadFileJob {
    private final String expectedHash;
    private final FileHelper mFileUtil;

    public ZincDownloadFileJob(final ZincRequestExecutor requestExecutor,
                               final URL url,
                               final File root,
                               final String child,
                               final boolean override,
                               final String expectedHash,
                               final FileHelper fileUtil) {
        super(requestExecutor, url, root, child, override);
        this.expectedHash = expectedHash;
        this.mFileUtil = fileUtil;
    }

    @Override
    protected void writeFile(final InputStream inputStream, final File file) throws IOException, ZincException {
        logMessage("Saving file " + file.getAbsolutePath());

        //todo: copy to temporary file, then move into final destination
        mFileUtil.streamToFile(inputStream, file, expectedHash);

        //mFileUtil.moveFile(temporaryFile, file);
    }
}
