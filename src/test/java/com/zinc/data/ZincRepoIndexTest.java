package com.zinc.data;

import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincRepoIndex;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;

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
}
