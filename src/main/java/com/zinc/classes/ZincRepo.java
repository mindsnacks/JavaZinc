package com.zinc.classes;

import com.zinc.classes.data.*;
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
    private final String mFlavorName;

    private final File mRoot;

    private final Map<SourceURL, Future<ZincCatalog>> mCatalogs = new HashMap<SourceURL, Future<ZincCatalog>>();
    private final Map<BundleID, Future<ZincBundle>> mBundles = new HashMap<BundleID, Future<ZincBundle>>();

    /**
     * @todo remove cached promises that failed?
     */
    public ZincRepo(final ZincFutureFactory jobFactory, final URI root, final ZincRepoIndexWriter repoIndexWriter, final String flavorName) {
        mJobFactory = jobFactory;
        mFlavorName = flavorName;
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
            getBundle(bundleID);
        }
    }

    public void addSourceURL(final SourceURL sourceURL) {
        if (mIndexWriter.getIndex().addSourceURL(sourceURL)) {
            mIndexWriter.saveIndex();
        }

        getCatalog(sourceURL);
    }

    public void startTrackingBundle(final BundleID bundleID, final String distribution) {
        if (mIndexWriter.getIndex().trackBundle(bundleID, distribution)) {
            mIndexWriter.saveIndex();
        }

        getBundle(bundleID);
    }

    private Future<ZincCatalog> getCatalog(final SourceURL sourceURL) {
        if (!mCatalogs.containsKey(sourceURL)) {
            mCatalogs.put(sourceURL, mJobFactory.downloadCatalog(sourceURL));
        }

        return mCatalogs.get(sourceURL);
    }

    /**
     * Returns the existing promise for this bundle, or creates a new one if it was not being tracked.
     */
    public Future<ZincBundle> getBundle(final BundleID bundleID) {
        if (!mBundles.containsKey(bundleID)) {
            mBundles.put(bundleID, cloneBundle(bundleID, getTrackingInfo(bundleID).getDistribution()));
        }

        return mBundles.get(bundleID);
    }

    private ZincRepoIndex.TrackingInfo getTrackingInfo(final BundleID bundleID) {
        try {
            return  mIndexWriter.getIndex().getTrackingInfo(bundleID);
        } catch (ZincRepoIndex.BundleNotBeingTrackedException e) {
            throw new ZincRuntimeException(e.getMessage(), e);
        }
    }

    private Future<ZincBundle> cloneBundle(final BundleID bundleID, final String distribution) {
        final String catalogID = bundleID.getCatalogID();
        final SourceURL sourceURL;

        try {
            sourceURL = mIndexWriter.getIndex().getSourceURLForCatalog(catalogID);
        } catch (ZincRepoIndex.CatalogNotFoundException e) {
            throw new ZincRuntimeException(String.format("No sources for catalog '%s'", catalogID));
        }

        return mJobFactory.cloneBundle(sourceURL, bundleID, distribution, mFlavorName, mRoot, getCatalog(sourceURL));
    }
}