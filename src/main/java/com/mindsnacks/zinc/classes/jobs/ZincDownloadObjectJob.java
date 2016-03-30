package com.mindsnacks.zinc.classes.jobs;

import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;

import java.io.File;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadObjectJob<V> extends AbstractZincDownloadJob<V> {
    private final Gson mGson;
    private final FileHelper mFileHelper;
    private final File mRepoFolder;

    public ZincDownloadObjectJob(final ZincRequestExecutor requestFactory,
                                 final URL url,
                                 final Gson gson,
                                 final FileHelper fileHelper,
                                 final File repoFolder,
                                 final Class<V> theClass) {
        super(requestFactory, url, theClass);
        mGson = gson;
        mFileHelper = fileHelper;
        mRepoFolder = repoFolder;
    }

    @Override
    public V run() throws Exception {
        File temporaryOutputFile = getTemporaryDownloadFolder(mUrl.getFile());
        if (temporaryOutputFile.exists()) {
            mFileHelper.removeFile(temporaryOutputFile);
        }
        mFileHelper.streamToFileWithoutValidation(mRequestExecutor.get(mUrl), temporaryOutputFile);
        File temporaryUncompressedFile = getTemporaryDownloadFolder(getJsonFileName());
        if (temporaryUncompressedFile.exists()) {
            mFileHelper.removeFile(temporaryUncompressedFile);
        }
        mFileHelper.unzipFileWithoutValidation(temporaryOutputFile, temporaryUncompressedFile);
        return mGson.fromJson(mFileHelper.readerForFile(temporaryUncompressedFile), mClass);
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " <" + mClass.getSimpleName() + ">";
    }

    private String getJsonFileName() {
        String gzippedFileName = mUrl.getFile();
        return gzippedFileName.substring(0, gzippedFileName.lastIndexOf(FileHelper.GZIPPED_FORMAT) - 1);
    }

    private File getTemporaryDownloadFolder(final String fileName) {
        return new File(mRepoFolder,
                PathHelper.getLocalTemporaryDownloadFolder(fileName)
        );
    }
}
