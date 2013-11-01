package com.mindsnacks.zinc.classes;

import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.downloads.PriorityJobQueue;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepo implements Repo {
    private final PriorityJobQueue<ZincCloneBundleRequest, ZincBundle> mQueue;
    private final ZincRepoIndexWriter mIndexWriter;
    private final ZincCatalogsCache mCatalogsCache;
    private final String mFlavorName;

    private final File mRoot;

    private final Map<BundleID, ZincCloneBundleRequest> mBundles = new HashMap<BundleID, ZincCloneBundleRequest>();

    /**
     * @todo remove cached promises that failed?
     * Instances are paused after initialization. You must call `start` after tracking the bundles.
     */
    public ZincRepo(final PriorityJobQueue<ZincCloneBundleRequest, ZincBundle> queue,
                    final URI root,
                    final ZincRepoIndexWriter repoIndexWriter,
                    final ZincCatalogsCache catalogsCache,
                    final String flavorName) {
        mQueue = queue;
        mCatalogsCache = catalogsCache;
        mFlavorName = flavorName;
        mRoot = new File(root);
        mIndexWriter = repoIndexWriter;

        cloneTrackedBundles();
    }

    @Override
    public void start() {
        mQueue.start();
    }

    @Override
    public void pause() throws InterruptedException {
        mQueue.stop();
    }

    @Override
    public void addSourceURL(final SourceURL sourceURL) {
        if (mIndexWriter.getIndex().addSourceURL(sourceURL)) {
            mIndexWriter.saveIndex();
        }
    }

    @Override
    public void startTrackingBundle(final BundleID bundleID, final String distribution) {
        if (mIndexWriter.getIndex().trackBundle(bundleID, distribution)) {
            mIndexWriter.saveIndex();
        }

        cloneBundle(bundleID, distribution);
    }

    /**
     * Warning: calling this method will block if the clone task
     * has not been scheduled yet and the repo is paused.
     */
    @Override
    public Future<ZincBundle> getBundle(final BundleID bundleID) {
        try {
            return mQueue.get(mBundles.get(bundleID));
        } catch (PriorityJobQueue.JobNotFoundException e) {
            throw new ZincRuntimeException(String.format("Bundle '%s' was not being tracked", bundleID), e);
        }
    }

    @Override
    public void recalculatePriorities() {
        mQueue.recalculatePriorities();
    }

    private void cloneTrackedBundles() {
        final ZincRepoIndex index = mIndexWriter.getIndex();

        for (final BundleID bundleID : index.getTrackedBundleIDs()) {
            cloneBundle(bundleID, index.getTrackingInfo(bundleID).getDistribution());
        }
    }

    private void cloneBundle(final BundleID bundleID, final String distribution) {
        final String catalogID = bundleID.getCatalogID();
        final SourceURL sourceURL;

        try {
            sourceURL = mIndexWriter.getIndex().getSourceURLForCatalog(catalogID);
        } catch (ZincRepoIndex.CatalogNotFoundException e) {
            throw new ZincRuntimeException(String.format("No sources for catalog '%s'", catalogID));
        }

        final ZincCloneBundleRequest cloneBundleRequest = new ZincCloneBundleRequest(
                sourceURL,
                bundleID,
                distribution,
                mFlavorName,
                mRoot);

        mQueue.add(cloneBundleRequest);
        mBundles.put(bundleID, cloneBundleRequest);
    }
}