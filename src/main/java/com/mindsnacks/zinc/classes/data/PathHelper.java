package com.mindsnacks.zinc.classes.data;

/**
 * User: NachoSoto
 * Date: 10/7/13
 */
public class PathHelper {
    private static final String DOWNLOADS_FOLDER = "downloads";
    private static final String BUNDLES_FOLDER = "bundles";
    private static final String TEMPORARY_BUNDLES_FOLDER = "temp";

    private static final String CATALOGS_FOLDER = "catalogs";

    private static final String CATALOGS_FORMAT = "json";
    public static final String FLAVOR_SEPARATOR = "~";

    public static String getLocalDownloadFolder(final BundleID bundleID, final int version, final String flavorName) {
        return String.format("%s/%s-%d%s%s", DOWNLOADS_FOLDER, bundleID, version, FLAVOR_SEPARATOR, flavorName);
    }

    public static String getLocalBundleFolder(final BundleID bundleID, final int version, final String flavorName) {
        return String.format("%s/%s-%d%s%s", BUNDLES_FOLDER, bundleID, version, FLAVOR_SEPARATOR, flavorName);
    }

    public static String getLocalTemporaryBundleFolder(final BundleID bundleID, final int version, final String flavorName) {
        return String.format("%s/%s-%d%s%s", TEMPORARY_BUNDLES_FOLDER, bundleID, version, FLAVOR_SEPARATOR, flavorName);
    }

    public static String getLocalCatalogFilePath(final String catalogID) {
        return String.format("%s%s.%s", getCatalogsFolder(), catalogID, CATALOGS_FORMAT);
    }

    public static String getCatalogsFolder() {
        return String.format("%s/", CATALOGS_FOLDER);
    }
}
