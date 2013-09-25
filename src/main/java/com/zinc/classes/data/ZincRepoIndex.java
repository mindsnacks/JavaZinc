package com.zinc.classes.data;

import com.google.gson.annotations.SerializedName;
import com.zinc.exceptions.ZincException;
import com.zinc.exceptions.ZincRuntimeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: NachoSoto
 * Date: 9/3/13
 * @todo: deal with bundle ID, bundle name and catalog name
 */
public class ZincRepoIndex {
    @SerializedName("sources")
    final private Set<SourceURL> mSources = new HashSet<SourceURL>();

    @SerializedName("bundles")
    final private Map<String, TrackingInfo> mBundles = new HashMap<String, TrackingInfo>();

    public Set<SourceURL> getSources() {
        return mSources;
    }

    /**
     * @return true if sourceURL was added. false if it was already there.
     */
    public boolean addSourceURL(final SourceURL sourceURL) {
        if (!mSources.contains(sourceURL)) {
            mSources.add(sourceURL);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if bundle was added or the distribution changed.
     */
    public boolean trackBundle(final BundleID bundleID, final String distribution) {
        final String key = bundleID.toString();

        if (!mBundles.containsKey(key) || !mBundles.get(key).getDistribution().equals(distribution)) {
            mBundles.put(key, new TrackingInfo(distribution));
            return true;
        } else {
            return false;
        }
    }

    public TrackingInfo getTrackingInfo(final BundleID bundleID) throws BundleNotBeingTrackedException {
        final String key = bundleID.toString();

        if (mBundles.containsKey(key)) {
            return mBundles.get(key);
        } else {
            throw new BundleNotBeingTrackedException(bundleID);
        }
    }

    public Set<BundleID> getTrackedBundleIDs() {
        final HashSet<BundleID> result = new HashSet<BundleID>();

        for (final String bundle : mBundles.keySet()) {
            result.add(new BundleID(bundle));
        }

        return result;
    }

    public SourceURL getSourceURLForCatalog(final String catalogID) throws CatalogNotFoundException {
       for (final SourceURL url : mSources) {
           if (url.getCatalogID().equals(catalogID)) {
               return url;
           }
       }

       throw new CatalogNotFoundException(catalogID);
    }

    @Override
    public String toString() {
        return "ZincRepoIndex {" +
                "mSources=" + mSources +
                '}';
    }

    public static class TrackingInfo {
        @SerializedName("distribution")
        final private String mDistribution;

        public TrackingInfo(final String distribution) {
            mDistribution = distribution;
        }

        public String getDistribution() {
            return mDistribution;
        }
    }

    public static class CatalogNotFoundException extends ZincException {
        public CatalogNotFoundException(final String catalogID) {
            super(String.format("Source URL for catalog '%s' not found", catalogID));
        }
    }

    public static class BundleNotBeingTrackedException extends ZincRuntimeException {
        public BundleNotBeingTrackedException(final BundleID bundleID) {
            super(String.format("Bundle '%s' is not currently being tracked", bundleID));
        }
    }
}
