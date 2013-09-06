package com.zinc;

import com.google.gson.Gson;
import com.zinc.classes.ZincCatalog;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ZincCatalogTest extends ZincBaseTest {
    private Gson gson;

    @Before
    public void setUp() throws Exception {
        gson = createGson();
    }

    @Test
    public void createFromJSON() throws ZincCatalog.DistributionNotFoundException {
        String json = "{\n" +
                "  \"bundles\": {\n" +
                "    \"bundleID1\": {\n" +
                "      \"distributions\": {\n" +
                "        \"master\": 1,\n" +
                "        \"develop\": 2\n" +
                "      }\n" +
                "    },\n" +
                "    \"bundleID2\": {\n" +
                "      \"distributions\": {\n" +
                "        \"master\": 2,\n" +
                "        \"develop\": 3\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"id\": \"repo\"\n" +
                "}";

        final ZincCatalog catalog = gson.fromJson(json, ZincCatalog.class);

        assertEquals("repo", catalog.getIdentifier());
        assertEquals(1, catalog.getVersionForBundleID("bundleID1", "master"));
        assertEquals(2, catalog.getVersionForBundleID("bundleID1", "develop"));
        assertEquals(2, catalog.getVersionForBundleID("bundleID2", "master"));
        assertEquals(3, catalog.getVersionForBundleID("bundleID2", "develop"));
    }

    @Test(expected = ZincCatalog.DistributionNotFoundException.class)
    public void throwsIfDistributionIsNotFound() throws ZincCatalog.DistributionNotFoundException {
        final String bundleID = "com.mindsnacks.games.swell";
        final ZincCatalog catalog = initializeSampleCatalog(bundleID);

        // run
        catalog.getVersionForBundleID(bundleID, "nonexistent distro");
    }

    @Test(expected = ZincCatalog.DistributionNotFoundException.class)
    public void throwsIfBundleIsNotFound() throws ZincCatalog.DistributionNotFoundException {
        final ZincCatalog catalog = initializeSampleCatalog("com.mindsnacks.games.swell");

        // run
        catalog.getVersionForBundleID("some other bundle", "master");
    }

    private ZincCatalog initializeSampleCatalog(final String bundleID) {
        final Map<String, Integer> distributions = new HashMap<String, Integer>();
        distributions.put("master", 2);

        final Map<String, ZincCatalog.Info> bundles = new HashMap<String, ZincCatalog.Info>();
        bundles.put(bundleID, new ZincCatalog.Info(distributions));

        return new ZincCatalog("identifier", bundles);
    }
}