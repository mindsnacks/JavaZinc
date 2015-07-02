package com.mindsnacks.zinc.classes.data;

/**
 * @author NachoSoto
 * Date: 10/7/13
 * @todo convert to instance methods.
 */
public class PathHelper {
    private static final String DOWNLOADS_FOLDER = "downloads";
    private static final String BUNDLES_FOLDER = "bundles";
    private static final String TEMPORARY_BUNDLES_FOLDER = "temp";

    private static final String CATALOGS_FOLDER = "catalogs";
    private static final String CATALOGS_FORMAT = "json";

    private static final String MANIFESTS_FOLDER = "manifests";
    private static final String MANIFESTS_FORMAT = "json";

    public static final String VERSION_SEPARATOR = "-";
    public static final String FLAVOR_SEPARATOR = "~";

    public static String getLocalDownloadFolder(final BundleID bundleID, final int version, final String flavorName) {
        return String.format("%s/%s%s%d%s%s", DOWNLOADS_FOLDER, bundleID, VERSION_SEPARATOR, version, FLAVOR_SEPARATOR, flavorName);
    }

    public static String getLocalBundleFolder(final BundleID bundleID, final int version, final String flavorName) {
        return String.format("%s/%s%s%d%s%s", BUNDLES_FOLDER, bundleID, VERSION_SEPARATOR, version, FLAVOR_SEPARATOR, flavorName);
    }

    public static String getLocalTemporaryBundleFolder(final BundleID bundleID, final int version, final String flavorName) {
        return String.format("%s/%s%s%d%s%s", TEMPORARY_BUNDLES_FOLDER, bundleID, VERSION_SEPARATOR, version, FLAVOR_SEPARATOR, flavorName);
    }

    public static String getLocalTemporaryDownloadFolder(final String name) {
        return String.format("%s/%s", TEMPORARY_BUNDLES_FOLDER, name);
    }

    public static String getLocalCatalogFilePath(final String catalogID) {
        return String.format("%s%s.%s", getCatalogsFolder(), catalogID, CATALOGS_FORMAT);
    }

    public static String getLocalManifestFilePath(final String catalogID, final String manifestID) {
        return String.format("%s%s.%s", getManifestsFolder(catalogID), manifestID, MANIFESTS_FORMAT);
    }

    public static String getCatalogsFolder() {
        return String.format("%s/", CATALOGS_FOLDER);
    }

    public static String getManifestsFolder() {
        return String.format("%s/", MANIFESTS_FOLDER);
    }

    public static String getBundlesFolder() { return String.format("%s/", BUNDLES_FOLDER);  }

    public static String getManifestsFolder(final String catalogID) { return String.format("%s%s/", getManifestsFolder(), catalogID); }

    public static String getManifestID(final String bundleName,
                                       final int version) {
        return String.format("%s%s%d", bundleName, VERSION_SEPARATOR, version);
    }

    public static String getBundleName(final String manifestFilename) {
        return manifestFilename.substring(0, manifestFilename.lastIndexOf(VERSION_SEPARATOR));
    }

    public static int getBundleVersion(final String manifestFilename) {
        return Integer.parseInt(manifestFilename.substring(manifestFilename.lastIndexOf(VERSION_SEPARATOR) + 1,
                                                           manifestFilename.lastIndexOf(MANIFESTS_FORMAT) - 1));
    }

    public static String getBundleID(final String bundleFolder) {
        String bundleFilenameWithoutFlavor = bundleFolder.substring(0, bundleFolder.lastIndexOf(FLAVOR_SEPARATOR));
        return bundleFilenameWithoutFlavor.substring(0, bundleFilenameWithoutFlavor.lastIndexOf(VERSION_SEPARATOR));
    }
}
