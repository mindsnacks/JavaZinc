package com.zinc.classes;

import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincCatalog;
import com.zinc.classes.data.ZincRepoIndex;
import com.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.net.URI;
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

    private final Map<SourceURL, Future<ZincCatalog>> mCatalogs = new HashMap<SourceURL, Future<ZincCatalog>>();

    public ZincRepo(final ZincFutureFactory jobFactory, final URI root, final ZincRepoIndexWriter repoIndexWriter) {
        mJobFactory = jobFactory;
        mRoot = new File(root);
        mIndexWriter = repoIndexWriter;

        downloadCatalogsForTrackedSources();
        downloadTrackedBundles();
    }

    private void downloadCatalogsForTrackedSources() {
        for (final SourceURL sourceURL : mIndexWriter.getIndex().getSources()) {
            getCatalog(sourceURL);
        }
    }

    private void downloadTrackedBundles() {
        final ZincRepoIndex index = mIndexWriter.getIndex();

        for (final BundleID bundleID : index.getTrackedBundleIDs()) {
            cloneBundle(bundleID, index.getTrackingInfo(bundleID).getDistribution());
        }
    }

    /**
     * @todo remove all cached bundle promises that failed?
     */
    public void addSourceURL(final SourceURL sourceURL) {
        mIndexWriter.getIndex().addSourceURL(sourceURL);
        mIndexWriter.saveIndex();

        getCatalog(sourceURL);
    }

    public void startTrackingBundle(final BundleID bundleID, final String distribution) {
        mIndexWriter.getIndex().trackBundle(bundleID, distribution);
        mIndexWriter.saveIndex();

        cloneBundle(bundleID, distribution);
    }

    private Future<ZincCatalog> getCatalog(final SourceURL sourceURL) {
        if (!mCatalogs.containsKey(sourceURL)) {
            mCatalogs.put(sourceURL, mJobFactory.downloadCatalog(sourceURL));
        }

        return mCatalogs.get(sourceURL);
    }

    private void cloneBundle(final BundleID bundleID, final String distribution) {
        final String catalogID = bundleID.getCatalogID();

        final SourceURL sourceURL;
        try {
            sourceURL = mIndexWriter.getIndex().getSourceURLForCatalog(catalogID);
        } catch (ZincRepoIndex.CatalogNotFoundException e) {
            throw new ZincRuntimeException(String.format("No sources for catalog '%s'", catalogID));
        }

        mJobFactory.cloneBundle(sourceURL, bundleID, distribution, getCatalog(sourceURL), mRoot);
    }
}