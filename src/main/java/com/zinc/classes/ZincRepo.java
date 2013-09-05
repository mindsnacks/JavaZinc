package com.zinc.classes;

import com.zinc.classes.jobs.AbstractZincJob;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepo {
    private final ExecutorService mExecutorService;
    private final ZincJobFactory mJobFactory;
    
    private final File mRoot;

    private final ZincRepoIndexWriter mIndexWriter;

    private final Map<URL, Future<ZincCatalog>> mCatalogs;

    public ZincRepo(final ExecutorService executorService, final ZincJobFactory jobFactory, final URI root, final ZincRepoIndexWriter repoIndexWriter) {
        mExecutorService = executorService;
        mJobFactory = jobFactory;
        mRoot = new File(root);
        mIndexWriter = repoIndexWriter;

        mCatalogs = new HashMap<URL, Future<ZincCatalog>>();
    }

    public void addSourceURL(final URL sourceURL) {
        mIndexWriter.getIndex().addSourceURL(sourceURL);
        mIndexWriter.saveIndex();

        downloadCatalog(sourceURL);
    }

    public void startTrackingBundle(final String bundleID, final String distribution) {
        mIndexWriter.getIndex().trackBundle(bundleID, distribution);
        mIndexWriter.saveIndex();
    }

    private void downloadCatalog(final URL url) {
        if (!mCatalogs.containsKey(url)) {
            mCatalogs.put(url, executeJob(mJobFactory.downloadCatalog(url)));
        }
    }

    private <V> Future<V> executeJob(final AbstractZincJob<V> job) {
        return mExecutorService.submit(job);
    }

    public static interface ZincJobFactory {
        AbstractZincJob<ZincCatalog> downloadCatalog(final URL sourceURL);
    }
}