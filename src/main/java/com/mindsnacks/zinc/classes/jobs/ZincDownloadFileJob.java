package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.classes.fileutils.ValidatingDigestOutputStream;

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
    private final File mRepoFolder;

    public ZincDownloadFileJob(final ZincRequestExecutor requestExecutor,
                               final URL url,
                               final File root,
                               final File repoFolder,
                               final String child,
                               final boolean override,
                               final String expectedHash,
                               final FileHelper fileUtil) {
        super(requestExecutor, url, root, child, override);
        this.expectedHash = expectedHash;
        this.mFileUtil = fileUtil;
        this.mRepoFolder = repoFolder;
    }

    @Override
    protected void writeFile(final InputStream inputStream, final File file) throws IOException, ValidatingDigestOutputStream.HashFailedException {
        logMessage("Saving file " + file.getAbsolutePath());

        File temporaryFile = new File(
                getTemporaryDownloadFolder(file.getName()),
                file.getName());

        mFileUtil.streamToFile(inputStream, temporaryFile, expectedHash);
        mFileUtil.moveFile(temporaryFile, file);
    }

    private File getTemporaryDownloadFolder(final String fileName) {
        return new File(mRepoFolder,
                PathHelper.getLocalTemporaryDownloadFolder(fileName)
        );
    }
}
