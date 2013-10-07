package com.mindsnacks.zinc.classes.data;

/**
 * User: NachoSoto
 * Date: 10/7/13
 */
public class PathHelper {
    private static final String DOWNLOADS_FOLDER = "downloads";
    private static final String BUNDLES_FOLDER = "bundles";

    public static final String FLAVOR_SEPARATOR = "~";

    public static String getLocalDownloadsFolder(final BundleID bundleID, final int version, final String flavorName) {
        return String.format("%s/%s-%d%s%s", DOWNLOADS_FOLDER, bundleID, version, FLAVOR_SEPARATOR, flavorName);
    }

    public static String getLocalBundlesFolder(final BundleID bundleID, final int version, final String flavorName) {
        return String.format("%s/%s-%d%s%s", BUNDLES_FOLDER, bundleID, version, FLAVOR_SEPARATOR, flavorName);
    }
}
