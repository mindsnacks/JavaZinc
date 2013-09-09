package com.zinc.classes;

import com.zinc.classes.data.ZincCatalog;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepo {
    private final ZincFutureFactory mJobFactory;
    private final ZincRepoIndexWriter mIndexWriter;

    private final File mRoot;

    private final Map<URL, Future<ZincCatalog>> mCatalogs = new HashMap<URL, Future<ZincCatalog>>();

    public ZincRepo(final ZincFutureFactory jobFactory, final URI root, final ZincRepoIndexWriter repoIndexWriter) {
        mJobFactory = jobFactory;
        mRoot = new File(root);
        mIndexWriter = repoIndexWriter;
    }

    /**
     * @todo remove all cached bundle promises that failed?
     */
    public void addSourceURL(final URL sourceURL) {
        mIndexWriter.getIndex().addSourceURL(sourceURL);
        mIndexWriter.saveIndex();

        downloadCatalog(sourceURL);
    }

    public void startTrackingBundle(final String bundleID, final String distribution) {
        mIndexWriter.getIndex().trackBundle(bundleID, distribution);
        mIndexWriter.saveIndex();
    }

    private void downloadCatalog(final URL sourceURL) {
        if (!mCatalogs.containsKey(sourceURL)) {
            mCatalogs.put(sourceURL, mJobFactory.downloadCatalog(sourceURL));
        }
    }
}