package com.mindsnacks.zinc.classes.data;

/**
 * User: NachoSoto
 * Date: 9/9/13
 */
public class BundleID {
    private final static String SEPARATOR = ".";

    private final String mCatalogID;
    private final String mBundleName;

    public BundleID(final String bundleID) {
        this(extractCatalogID(bundleID), extractBundleName(bundleID));
    }

    public BundleID(final String catalogID, final String bundleName) {
        mCatalogID = catalogID;
        mBundleName = bundleName;
    }

    public String getCatalogID() {
        return mCatalogID;
    }

    public String getBundleName() {
        return mBundleName;
    }

    @Override
    public String toString() {
        return getCatalogID() + SEPARATOR + getBundleName();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return o.toString().equals(toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    private static String extractBundleName(final String bundleID) {
        return bundleID.substring(bundleID.lastIndexOf(SEPARATOR) + 1);
    }

    private static String extractCatalogID(final String bundleID) {
        return bundleID.substring(0, bundleID.lastIndexOf(SEPARATOR));
    }
}
