package com.zinc;

import org.junit.Before;
import org.junit.Test;
import com.zinc.utils.ZincBaseTest;
import com.zinc.classes.ZincRepoIndex;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;

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
        final URL url = new URL("http://mindsnacks.com");

        // run
        index.addSourceURL(url);

        // verify
        assertEquals(new HashSet<URL>(Arrays.asList(url)), index.getSources());
    }

    @Test
    public void trackBundleAddsTrackingInfo() throws Exception {
        final String bundleID = "com.mindsnacks.games.swell";
        final String distribution = "master";

        // run
        index.trackBundle(bundleID, distribution);

        // verify
        assertEquals(distribution, index.getTrackingInfo(bundleID).getDistribution());
    }

    @Test
    public void trackBundleUpdatesDistribution() throws Exception {
        final String bundleID = "com.mindsnacks.games.swell";
        final String oldDistribution = "master",
                     newDistribution = "develop";

        // run
        index.trackBundle(bundleID, oldDistribution);
        index.trackBundle(bundleID, newDistribution);

        // verify
        assertEquals(newDistribution, index.getTrackingInfo(bundleID).getDistribution());
    }

    // TODO: serialization tests
}
