package com.mindsnacks.zinc.data;

import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.data.ZincCatalog;
import com.mindsnacks.zinc.utils.ZincBaseTest;
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
        final String json = "{\n" +
                "  \"bundles\": {\n" +
                "    \"bundleName1\": {\n" +
                "      \"distributions\": {\n" +
                "        \"master\": 1,\n" +
                "        \"develop\": 2\n" +
                "      }\n" +
                "    },\n" +
                "    \"bundleName2\": {\n" +
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
        assertEquals(1, catalog.getVersionForBundleName("bundleName1", "master"));
        assertEquals(2, catalog.getVersionForBundleName("bundleName1", "develop"));
        assertEquals(2, catalog.getVersionForBundleName("bundleName2", "master"));
        assertEquals(3, catalog.getVersionForBundleName("bundleName2", "develop"));
    }

    @Test(expected = ZincCatalog.DistributionNotFoundException.class)
    public void throwsIfDistributionIsNotFound() throws ZincCatalog.DistributionNotFoundException {
        final String bundleName = "swell";
        final ZincCatalog catalog = initializeSampleCatalog(bundleName);

        // run
        catalog.getVersionForBundleName(bundleName, "nonexistent distro");
    }

    @Test(expected = ZincCatalog.DistributionNotFoundException.class)
    public void throwsIfBundleIsNotFound() throws ZincCatalog.DistributionNotFoundException {
        final ZincCatalog catalog = initializeSampleCatalog("com.mindsnacks.games.swell");

        // run
        catalog.getVersionForBundleName("some other bundle", "master");
    }

    private ZincCatalog initializeSampleCatalog(final String bundleName) {
        final Map<String, Integer> distributions = new HashMap<String, Integer>();
        distributions.put("master", 2);

        final Map<String, ZincCatalog.Info> bundles = new HashMap<String, ZincCatalog.Info>();
        bundles.put(bundleName, new ZincCatalog.Info(distributions));

        return new ZincCatalog("identifier", bundles);
    }
}