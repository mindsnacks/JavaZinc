package com.zinc.classes.data;

import com.google.gson.annotations.SerializedName;

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
}
