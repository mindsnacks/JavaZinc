package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.*;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadBundleJob extends ZincJob<ZincBundle> {
    private final SourceURL mSourceURL;
    private final BundleID mBundleID;
    private final String mDistribution;
    private final String mFlavorName;
    private final Future<ZincCatalog> mCatalog;
    private final File mRepoFolder;

    protected final ZincFutureFactory mFutureFactory;

    public ZincDownloadBundleJob(final ZincBundleCloneRequest zincBundleCloneRequest,
                                 final Future<ZincCatalog> catalogFuture,
                                 final ZincFutureFactory futureFactory) {
        assert zincBundleCloneRequest.getSourceURL().getCatalogID().equals(zincBundleCloneRequest.getBundleID().getCatalogID());

        mSourceURL = zincBundleCloneRequest.getSourceURL();
        mBundleID = zincBundleCloneRequest.getBundleID();
        mDistribution = zincBundleCloneRequest.getDistribution();
        mFlavorName = zincBundleCloneRequest.getFlavorName();
        mCatalog = catalogFuture;
        mFutureFactory = futureFactory;
        mRepoFolder = zincBundleCloneRequest.getRepoFolder();
    }

    @Override
    public ZincBundle run() throws Exception {
        final ZincCatalog catalog = mCatalog.get();

        final String bundleName = mBundleID.getBundleName();
        final int version = catalog.getVersionForBundleName(bundleName, mDistribution);

        final URL archiveURL = mSourceURL.getArchiveURL(bundleName, version, mFlavorName);
        final String folderName = SourceURL.getLocalDownloadsFolder(bundleName, version, mFlavorName);

        final Future<File> job = mFutureFactory.downloadArchive(archiveURL, mRepoFolder, folderName, false);

        return new ZincBundle(job.get(), mBundleID, version);
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mBundleID + ")";
    }
}
