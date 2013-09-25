package com.zinc.classes.data;

import java.io.File;

public class ZincCloneBundleRequest {
    private final SourceURL mSourceURL;
    private final BundleID mBundleID;
    private final String mDistribution;
    private final String mFlavorName;
    private final File mRepoFolder;

    public ZincCloneBundleRequest(final SourceURL sourceURL,
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ZincCloneBundleRequest that = (ZincCloneBundleRequest) o;

        return (mBundleID.equals(that.mBundleID) &&
                mDistribution.equals(that.mDistribution) &&
                mFlavorName.equals(that.mFlavorName) &&
                mRepoFolder.equals(that.mRepoFolder) &&
                mSourceURL.equals(that.mSourceURL));

    }

    @Override
    public int hashCode() {
        int result = mSourceURL.hashCode();
        result = 31 * result + mBundleID.hashCode();
        result = 31 * result + mDistribution.hashCode();
        result = 31 * result + mFlavorName.hashCode();
        result = 31 * result + mRepoFolder.hashCode();

        return result;
    }
}
