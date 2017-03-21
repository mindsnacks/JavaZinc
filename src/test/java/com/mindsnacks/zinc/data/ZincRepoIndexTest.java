package com.mindsnacks.zinc.data;

import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.SourceURL;
import com.mindsnacks.zinc.classes.data.ZincRepoIndex;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

/**
 * User: NachoSoto
 * Date: 9/4/13
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

        // run
        index.trackBundle(bundleID, distribution);

        // verify
        assertEquals(distribution, index.getTrackingInfo(bundleID).getDistribution());
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
    public void stopTrackingBundle() throws Exception {
        final BundleID bundleID1 = new BundleID("com.mindsnacks.games.swell"),
                bundleID2 = new BundleID("com.mindsnacks.lessons.body-parts");

        // run
        index.trackBundle(bundleID1, "master");
        index.trackBundle(bundleID2, "develop");

        // verify
        assertEquals(new HashSet<BundleID>(Arrays.asList(bundleID1, bundleID2)), index.getTrackedBundleIDs());

        assertTrue(index.stopTrackingBundle(bundleID1, "master"));
        assertTrue(index.stopTrackingBundle(bundleID2, "develop"));

        assertEquals(new HashSet<BundleID>(), index.getTrackedBundleIDs());
    }

    @Test
    public void stopTrackingNotTrackingBundleReturnsFalse() throws Exception {
        final BundleID bundleID1 = new BundleID("com.mindsnacks.games.swell"),
                bundleID2 = new BundleID("com.mindsnacks.lessons.body-parts");

        index.trackBundle(bundleID1, "onedistribution");

        assertFalse(index.stopTrackingBundle(bundleID1, "anotherdistribution"));
        assertFalse(index.stopTrackingBundle(bundleID2, "onedistribution"));
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
