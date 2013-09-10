package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
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
    private final String mBundleName;
    private final String mDistribution;
    private final Future<ZincCatalog> mCatalog;
    private final ZincFutureFactory mFutureFactory;
    private final File mRepoFolder;

    public ZincCloneBundleJob(final SourceURL sourceURL,
                              final String bundleName,
                              final String distribution,
                              final Future<ZincCatalog> catalogFuture,
                              final ZincFutureFactory futureFactory,
                              final File repoFolder) {
        mSourceURL = sourceURL;
        mBundleName = bundleName;
        mDistribution = distribution;
        mCatalog = catalogFuture;
        mFutureFactory = futureFactory;
        mRepoFolder = repoFolder;
    }

    @Override
    public ZincBundle call() throws Exception {
        final ZincCatalog catalog = mCatalog.get();
        final int version = catalog.getVersionForBundleName(mBundleName, mDistribution);

        final String filename = String.format("%s/%s-%d", ARCHIVES_FOLDER, mBundleName, version);

        final Future<File> job = mFutureFactory.downloadArchive(new URL(mSourceURL.getUrl(), filename + ARCHIVES_FORMAT), mRepoFolder, filename);

        return new ZincBundle(job.get(), mBundleName);
    }
}
