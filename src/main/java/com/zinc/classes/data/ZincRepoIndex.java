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
     * @return true if bundle was added or the distribution or flavor changed.
     */
    public boolean trackBundle(final BundleID bundleID, final String distribution, final String flavor)  throws BundleFlavorChangedException {
        final String key = bundleID.toString();
        final boolean containsKey = mBundles.containsKey(key);

        if (containsKey) {
            final String existingFlavor = mBundles.get(key).getFlavor();
            if (existingFlavor != null && (flavor == null || !existingFlavor.equals(flavor))) {
                throw new BundleFlavorChangedException(bundleID, mBundles.get(key).getFlavor(), flavor);
            }
        }

        if (!containsKey
                || !mBundles.get(key).getDistribution().equals(distribution)
                || (mBundles.get(key).getFlavor() == null && flavor != null)) {

            mBundles.put(key, new TrackingInfo(distribution, flavor));
            return true;

        } else {
            return false;
        }
    }

    public boolean trackBundle(final BundleID bundleID, final String distribution) throws BundleFlavorChangedException {
        return trackBundle(bundleID, distribution, null);
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

        @SerializedName("flavor")
        final private String mFlavor;

        public TrackingInfo(final String distribution, final String flavor) {
            mDistribution = distribution;
            mFlavor = flavor;
        }

        public TrackingInfo(final String distribution) {
            mDistribution = distribution;
            mFlavor = null;
        }

        public String getDistribution() {
            return mDistribution;
        }

        public String getFlavor() {
            return mFlavor;
        }
    }

    public static class CatalogNotFoundException extends ZincException {
        public CatalogNotFoundException(final String catalogID) {
            super(String.format("Source URL for catalog '%s' not found", catalogID));
        }
    }

    public static class BundleNotBeingTrackedException extends ZincException {
        public BundleNotBeingTrackedException(final BundleID bundleID) {
            super(String.format("Bundle '%s' is not currently being tracked", bundleID));
        }
    }

    public static class BundleFlavorChangedException extends ZincRuntimeException {
        public BundleFlavorChangedException(final BundleID bundleID,
                                            final String existingFlavor,
                                            final String newFlavor) {
            super(String.format("Bundle '%s' is already tracking flavor '%s', new flavor '%s'",
                    bundleID, existingFlavor, newFlavor));
        }
    }
}
