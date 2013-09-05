package com.zinc.classes.jobs;

import com.zinc.classes.ZincBundle;
import com.zinc.classes.ZincCatalog;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincBundleCloneJob extends AbstractZincJob<ZincBundle> {
    private static final String ARCHIVES_FOLDER = "archives";

    private final URL mSourceURL;
    private final String mBundleID;
    private final Future<ZincCatalog> mZincCatalog;
    private final Future<File> mArchiveFolder;

    public ZincBundleCloneJob(final URL sourceURL, final String bundleID, final Future<ZincCatalog> zincCatalog, final Future<File> archiveFolder) {
        mSourceURL = sourceURL;
        mBundleID = bundleID;
        mZincCatalog = zincCatalog;
        mArchiveFolder = archiveFolder;
    }

    @Override
    public ZincBundle call() throws Exception {

        return null;
//      final URL url = new URL(mSourceURL, ARCHIVES_FOLDER + "/" + mBundleID + "-" + mVersion + "." + ARCHIVES_EXTENSION);
    }
}
