package com.zinc.classes.data;

import java.io.File;

public class ZincBundleTrackingRequest {
    private final BundleID mBundleID;
    private final String mDistribution;
    private final String mFlavorName;

    public ZincBundleTrackingRequest(final BundleID bundleID,
                                     final String distribution,
                                     final String flavorName) {
        mBundleID = bundleID;
        mDistribution = distribution;
        mFlavorName = flavorName;
    }

    public BundleID getBundleID() {
        return mBundleID;
    }

    public String getDistribution() {
        return mDistribution;
    }

    public String getFlavorName() {
        return mFlavorName;
    }
}
