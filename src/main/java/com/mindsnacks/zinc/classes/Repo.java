package com.mindsnacks.zinc.classes;

import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.SourceURL;
import com.mindsnacks.zinc.classes.data.ZincBundle;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author NachoSoto
 */
public interface Repo {
    void start();

    void pause() throws InterruptedException;

    void addSourceURL(SourceURL sourceURL);

    void startTrackingBundle(BundleID bundleID, String distribution);

    void startTrackingBundles(List<BundleID> bundleIDs, String distribution);

    void stopTrackingBundles(Set<BundleID> bundleIDs, String distribution);

    Future<ZincBundle> getBundle(BundleID bundleID);

    Set<BundleID> getTrackedBundleIDs();

    void recalculatePriorities();

    /**
     * Must be called before calling start.
     */
    void clearCachedCatalogs();

    public boolean isBundleValid(final ZincBundle bundle);

    public void retrackBundle(final ZincBundle bundle);
}
