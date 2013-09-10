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
    private static final String ARCHIVES_FOLDER = "archives";
    private static final String ARCHIVES_FORMAT = ".tar";

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
        final int version = catalog.getVersionForBundleName(mBundleID.getBundleName(), mDistribution);

        final String filename = String.format("%s/%s-%d", ARCHIVES_FOLDER, mBundleID.getBundleName(), version);

        final Future<File> job = mFutureFactory.downloadArchive(new URL(mSourceURL.getUrl(), filename + ARCHIVES_FORMAT), mRepoFolder, filename);

        return new ZincBundle(job.get(), mBundleID);
    }
}
