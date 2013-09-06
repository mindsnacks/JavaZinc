package com.zinc.classes.jobs;

import com.zinc.classes.ZincBundle;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincFutureFactory;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincCloneBundleJob implements ZincJob<ZincBundle> {
    private static final String ARCHIVES_FOLDER = "archives";

    private final Set<URL> mSourceURLs;
    private final String mBundleID;
    private final String mDistribution;
    private final Future<ZincCatalog> mCatalog;
    private final ZincFutureFactory mFutureFactory;
    private final File mRepoFolder;

    public ZincCloneBundleJob(final Set<URL> sourceURLs,
                              final String bundleID,
                              final String distribution,
                              final Future<ZincCatalog> catalogFuture,
                              final ZincFutureFactory futureFactory,
                              final File repoFolder) {
        mSourceURLs = sourceURLs;
        mBundleID = bundleID;
        mDistribution = distribution;
        mCatalog = catalogFuture;
        mFutureFactory = futureFactory;
        mRepoFolder = repoFolder;
    }

    @Override
    public ZincBundle call() throws Exception {
        assert (mSourceURLs.size() > 0);

        final ZincCatalog catalog = mCatalog.get();
        final int version = catalog.getVersionForBundleID(mBundleID, mDistribution);

        final Iterator<URL> iter = mSourceURLs.iterator();

        while (iter.hasNext()) {
            final URL sourceURL = iter.next();
            final Future<File> job = mFutureFactory.downloadArchive(sourceURL, mRepoFolder, String.format("%s/%s-%d", ARCHIVES_FOLDER, mBundleID, version));

            try {
                return new ZincBundle(job.get(), mBundleID);
            } catch (ExecutionException e) {
                if (!iter.hasNext()) {
                     throw e;
                }
            }
        }

        assert false; // should never get here
        return null;
    }
}
