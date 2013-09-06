package com.zinc.classes.jobs;

import com.zinc.classes.ZincBundle;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincJobCreator;

import java.io.File;
import java.net.URL;
import java.util.List;
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
    private final ZincJobCreator mJobCreator;
    private final File mRepoFolder;

    public ZincCloneBundleJob(final List<URL> sourceURLs,
                              final String bundleID,
                              final String distribution,
                              final Future<ZincCatalog> zincCatalog,
                              final ZincJobCreator jobCreator,
                              final File repoFolder) {
        mSourceURLs = sourceURLs;
        mBundleID = bundleID;
        mDistribution = distribution;
        mZincCatalog = zincCatalog;
        mJobCreator = jobCreator;
        mRepoFolder = repoFolder;
    }

    @Override
    public ZincBundle call() throws Exception {
        final ZincCatalog catalog = mZincCatalog.get();
        final int version = catalog.getVersionForBundleID(mBundleID, mDistribution);

        final ZincJob<File> job = mJobCreator.downloadArchive(mSourceURLs.get(0), mRepoFolder, ARCHIVES_FOLDER + "/" + mBundleID);

        return new ZincBundle(job.call(), mBundleID);
    }
}
