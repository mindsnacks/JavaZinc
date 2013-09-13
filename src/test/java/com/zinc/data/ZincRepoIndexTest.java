package com.zinc.data;

import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincRepoIndex;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * User: NachoSoto
 * Date: 9/4/13
 * @todo: serialization tests
 */
public class ZincRepoIndexTest extends ZincBaseTest {
    private ZincRepoIndex index;

    @Before
    public void setUp() throws Exception {
        index = new ZincRepoIndex();
    }

    @Test
    public void addSourceURL() throws Exception {
        final SourceURL url = new SourceURL(new URL("http://mindsnacks.com"), "com.mindsnacks.test");

        // run
        index.addSourceURL(url);

        // verify
        assertEquals(new HashSet<SourceURL>(Arrays.asList(url)), index.getSources());
    }

    @Test
    public void trackBundleAddsTrackingInfo() throws Exception {
        final BundleID bundleID = new BundleID("com.mindsnacks.games.swell");
        final String distribution = "master";
        final String flavor = "iphone";

        // run
        index.trackBundle(bundleID, distribution, flavor);

        // verify
        assertEquals(distribution, index.getTrackingInfo(bundleID).getDistribution());
        assertEquals(flavor, index.getTrackingInfo(bundleID).getFlavor());
    }

    @Test
    public void trackBundleAllowsNullFlavor() throws Exception {
        final BundleID bundleID = new BundleID("com.mindsnacks.games.swell");
        final String distribution = "master";

        // run
        index.trackBundle(bundleID, distribution, null);

        // verify
        assertEquals(distribution, index.getTrackingInfo(bundleID).getDistribution());
        assertNull(index.getTrackingInfo(bundleID).getFlavor());
    }

    @Test
    public void trackBundleUpdatesDistribution() throws Exception {
        final BundleID bundleID = new BundleID("com.mindsnacks.games.swell");
        final String oldDistribution = "master",
                     newDistribution = "develop";

        // run
        index.trackBundle(bundleID, oldDistribution);
        index.trackBundle(bundleID, newDistribution);

        // verify
        assertEquals(newDistribution, index.getTrackingInfo(bundleID).getDistribution());
    }

    @Test
    public void trackBundleUpdatesFlavorIfNull() throws Exception {
        final BundleID bundleID = new BundleID("com.mindsnacks.games.swell");
        final String flavor = "ipad";

        // run
        index.trackBundle(bundleID, "master", null);
        index.trackBundle(bundleID, "master", flavor);

        // verify
        assertEquals(flavor, index.getTrackingInfo(bundleID).getFlavor());
    }

    @Test(expected=ZincRepoIndex.BundleFlavorChangedException.class)
    public void trackBundleThrowIfFlavorChanged() throws Exception {
        final BundleID bundleID = new BundleID("com.mindsnacks.games.swell");
        final String oldFlavor = "ipad",
                     newFlavor = "iphone";

        // run
        index.trackBundle(bundleID, "master", oldFlavor);
        index.trackBundle(bundleID, "master", newFlavor);
    }

    @Test
    public void getTrackedBundleIDs() throws Exception {
        final BundleID bundleID1 = new BundleID("com.mindsnacks.games.swell"),
                       bundleID2 = new BundleID("com.mindsnacks.lessons.body-parts");

        // run
        index.trackBundle(bundleID1, "master");
        index.trackBundle(bundleID2, "develop");

        // verify
        assertEquals(new HashSet<BundleID>(Arrays.asList(bundleID1, bundleID2)), index.getTrackedBundleIDs());
    }

    @Test
    public void sourceURLForCatalogID() throws Exception {
        final URL host = new URL("http://mindsnacks.com");
        final String catalogID = "com.mindsnacks.games";
        final SourceURL correctSourceURL = new SourceURL(host, catalogID);

        index.addSourceURL(new SourceURL(host, "com.mindsnacks.lessons"));
        index.addSourceURL(correctSourceURL);
        index.addSourceURL(new SourceURL(host, "com.mindsnacks.misc"));

        // run
        final SourceURL result = index.getSourceURLForCatalog(catalogID);

        assertEquals(correctSourceURL, result);
    }

    @Test(expected = ZincRepoIndex.CatalogNotFoundException.class)
    public void sourceURLNotFound() throws Exception {
        index.addSourceURL(new SourceURL(new URL("http://mindsnacks.com"), "com.mindsnacks.lessons"));

        // run
        index.getSourceURLForCatalog("not-found");
    }
}
