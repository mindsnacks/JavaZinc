package com.mindsnacks.zinc.data;

import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Test;

import static com.mindsnacks.zinc.utils.TestFactory.randomInt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
    public void localBundleFolderIsDifferentThanDownload() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        assertThat(PathHelper.getLocalBundleFolder(bundleID, version, flavorName), not(equalTo(PathHelper.getLocalDownloadFolder(bundleID, version, flavorName))));
    }

    @Test
    public void localCatalogFolder() throws Exception {
        final String catalogID = "com.mindsnacks.games";

        final String result = PathHelper.getLocalCatalogFilePath(catalogID);

        assertTrue(result.contains(catalogID));
        assertTrue(result.endsWith(".json"));
    }
}
