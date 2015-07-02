package com.mindsnacks.zinc.data;

import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Test;

import java.nio.file.Path;

import static com.mindsnacks.zinc.utils.TestFactory.randomInt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * User: NachoSoto
 * Date: 10/7/13
 */
public class PathHelperTest extends ZincBaseTest {
    private final String catalogID = "com.mindsnacks.games";

    @Test
    public void localDownloadFolder() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        final String result = PathHelper.getLocalDownloadFolder(bundleID, version, flavorName);

        assertTrue(result.contains(Integer.toString(version)));
        assertTrue(result.contains(bundleID.toString()));
        assertTrue(result.contains(flavorName));
    }

    @Test
    public void localBundleFolder() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        final String result = PathHelper.getLocalBundleFolder(bundleID, version, flavorName);

        assertTrue(result.contains(Integer.toString(version)));
        assertTrue(result.contains(bundleID.toString()));
        assertTrue(result.contains(flavorName));
    }

    @Test
    public void localTemporaryBundleFolder() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        final String result = PathHelper.getLocalTemporaryBundleFolder(bundleID, version, flavorName);

        assertTrue(result.contains(Integer.toString(version)));
        assertTrue(result.contains(bundleID.toString()));
        assertTrue(result.contains(flavorName));
    }

    @Test
    public void localBundleFolderIsDifferentThanDownload() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        assertThat(PathHelper.getLocalBundleFolder(bundleID, version, flavorName), not(equalTo(PathHelper.getLocalDownloadFolder(bundleID, version, flavorName))));
    }

    @Test
    public void localCatalogFilePath() throws Exception {
        final String catalogID = "com.mindsnacks.games";

        final String result = PathHelper.getLocalCatalogFilePath(catalogID);

        assertTrue(result.contains(catalogID));
        assertTrue(result.endsWith(".json"));
    }

    @Test
    public void localManifestFilePath() {
        final String catalogID = "com.mindsnacks.games";
        final String manifestID = "watto-1";

        final String result = PathHelper.getLocalManifestFilePath(catalogID, manifestID);

        assertTrue(result.contains(catalogID));
        assertTrue(result.contains(manifestID));
        assertTrue(result.endsWith(".json"));
    }

    @Test
    public void catalogsFolder() throws Exception {
        assertTrue(PathHelper.getCatalogsFolder().endsWith("/"));
    }

    @Test
    public void manifestsFolder() throws Exception {
        assertTrue(PathHelper.getManifestsFolder().endsWith("/"));
    }

    @Test
    public void manifestsFolderWithParams() throws Exception {
        assertTrue(PathHelper.getManifestsFolder("meh").endsWith("/"));
    }

    @Test
    public void getBundleName() {
        final String bundleName = "a-simple-random_Bundle-name";
        final String manifestFilename = String.format("%s.json", PathHelper.getManifestID(bundleName, 213));

        assertEquals(bundleName, PathHelper.getBundleName(manifestFilename));
    }

    @Test
    public void getBundleVersion() {
        final String bundleName = "a-simple-random_Bundle-name";
        final int version = 4245;
        final String manifestFilename = String.format("%s.json", PathHelper.getManifestID(bundleName, version));

        assertEquals(version, PathHelper.getBundleVersion(manifestFilename));
    }

    @Test
    public void getBundleID() {
        final BundleID bundleID = new BundleID("com.wonder.content3.sat-trol");
        final String bundleFolder = PathHelper.getLocalBundleFolder(bundleID, 4, "3x-Android");
        final String bundleFolderName = bundleFolder.substring(bundleFolder.indexOf("/") + 1);

        assertEquals(bundleID.toString(), PathHelper.getBundleID(bundleFolderName));
    }
}
