package com.zinc.classes.data;

import java.io.File;

public class ZincBundleCloneRequest {
    private final SourceURL mSourceURL;
    private final BundleID mBundleID;
    private final String mDistribution;
    private final String mFlavorName;
    private final File mRepoFolder;

    public ZincBundleCloneRequest(final SourceURL sourceURL,
                                  final BundleID bundleID,
                                  final String distribution,
                                  final String flavorName,
                                  final File repoFolder) {
        mSourceURL = sourceURL;
        mBundleID = bundleID;
        mDistribution = distribution;
        mFlavorName = flavorName;
        mRepoFolder = repoFolder;
    }

    public SourceURL getSourceURL() {
        return mSourceURL;
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

    public File getRepoFolder() {
        return mRepoFolder;
    }
}
