package com.mindsnacks.zinc.data;

import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Test;

import static com.mindsnacks.zinc.utils.MockFactory.randomInt;
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
    public void localDownloadsFolder() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        final String result = PathHelper.getLocalDownloadsFolder(bundleID, version, flavorName);

        assertTrue(result.contains(Integer.toString(version)));
        assertTrue(result.contains(bundleID.toString()));
        assertTrue(result.contains(flavorName));
    }

    @Test
    public void localBundlesFolder() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        final String result = PathHelper.getLocalBundlesFolder(bundleID, version, flavorName);

        assertTrue(result.contains(Integer.toString(version)));
        assertTrue(result.contains(bundleID.toString()));
        assertTrue(result.contains(flavorName));
    }

    @Test
    public void localBundlesFolderIsDifferentThanDownloads() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        assertThat(PathHelper.getLocalBundlesFolder(bundleID, version, flavorName), not(equalTo(PathHelper.getLocalDownloadsFolder(bundleID, version, flavorName))));
    }
}
