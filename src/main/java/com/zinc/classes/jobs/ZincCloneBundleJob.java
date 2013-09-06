package com.zinc.classes.jobs;

import com.zinc.classes.ZincBundle;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincFutureFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincCloneBundleJob implements ZincJob<ZincBundle> {
    private static final String ARCHIVES_FOLDER = "archives";

    private final List<URL> mSourceURLs;
    private final String mBundleID;
    private final String mDistribution;
    private final Future<ZincCatalog> mZincCatalog;
    private final ZincFutureFactory mFutureFactory;
    private final File mRepoFolder;

    public ZincCloneBundleJob(final List<URL> sourceURLs,
                              final String bundleID,
                              final String distribution,
                              final Future<ZincCatalog> zincCatalog,
                              final ZincFutureFactory futureFactory,
                              final File repoFolder) {
        mSourceURLs = sourceURLs;
        mBundleID = bundleID;
        mDistribution = distribution;
        mZincCatalog = zincCatalog;
        mFutureFactory = futureFactory;
        mRepoFolder = repoFolder;
    }

    @Override
    public ZincBundle call() throws Exception {
        assert (mSourceURLs.size() > 0);

        final ZincCatalog catalog = mZincCatalog.get();
        final int version = catalog.getVersionForBundleID(mBundleID, mDistribution);

        final ListIterator<URL> iter = mSourceURLs.listIterator();

        while (iter.hasNext()) {
            final URL sourceURL = iter.next();
            final Future<File> job = mFutureFactory.downloadArchive(sourceURL, mRepoFolder, ARCHIVES_FOLDER + "/" + mBundleID + "-" + version);

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
