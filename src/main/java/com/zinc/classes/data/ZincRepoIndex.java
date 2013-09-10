package com.zinc.classes.data;

import com.google.gson.annotations.SerializedName;
import com.zinc.exceptions.ZincException;

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

    public void addSourceURL(final SourceURL sourceURL) {
        mSources.add(sourceURL);
    }

    public void trackBundle(final BundleID bundleID, final String distribution) {
        mBundles.put(bundleID.toString(), new TrackingInfo(distribution));
    }

    public TrackingInfo getTrackingInfo(final BundleID bundleID) {
        return mBundles.get(bundleID.toString());
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
}
