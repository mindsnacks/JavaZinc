package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincCatalog;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincCloneBundleJob implements ZincJob<ZincBundle> {

    private final SourceURL mSourceURL;
    private final BundleID mBundleID;
    private final String mDistribution;
    private final Future<ZincCatalog> mCatalog;
    private final ZincFutureFactory mFutureFactory;
    private final File mRepoFolder;

    public ZincCloneBundleJob(final SourceURL sourceURL,
                              final BundleID bundleID,
                              final String distribution,
                              final Future<ZincCatalog> catalogFuture,
                              final ZincFutureFactory futureFactory,
                              final File repoFolder) {
        assert sourceURL.getCatalogID().equals(bundleID.getCatalogID());

        mSourceURL = sourceURL;
        mBundleID = bundleID;
        mDistribution = distribution;
        mCatalog = catalogFuture;
        mFutureFactory = futureFactory;
        mRepoFolder = repoFolder;
    }

    @Override
    public ZincBundle call() throws Exception {
        final ZincCatalog catalog = mCatalog.get();

        final String bundleName = mBundleID.getBundleName();
        final int version = catalog.getVersionForBundleName(bundleName, mDistribution);

        final URL archiveURL = mSourceURL.getArchiveURL(bundleName, version);
        final String folderName = archiveURL.getFile().substring(0, archiveURL.getFile().lastIndexOf("."));

        final Future<File> job = mFutureFactory.downloadArchive(archiveURL, mRepoFolder, folderName);

        return new ZincBundle(job.get(), mBundleID);
    }
}
